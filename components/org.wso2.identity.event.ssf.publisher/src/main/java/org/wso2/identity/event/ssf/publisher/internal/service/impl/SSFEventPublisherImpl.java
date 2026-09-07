package org.wso2.identity.event.ssf.publisher.internal.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherServerException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterException;
import org.wso2.identity.event.ssf.publisher.internal.component.ClientManager;
import org.wso2.identity.event.ssf.publisher.internal.component.SSFAdapterDataHolder;
import org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_ACTIVE_WEBHOOKS_RETRIEVAL;

/**
 * OSGi service for publishing CAEP/SSF events as signed Security Event Tokens.
 */
public class SSFEventPublisherImpl implements EventPublisher {

    private static final Log log = LogFactory.getLog(SSFEventPublisherImpl.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);

    @Override
    public String getAssociatedAdapter() {

        return SSFAdapterConstants.SSF_ADAPTER_NAME;
    }

    @Override
    public List<String> getSupportedEventProfiles() {

        return Collections.singletonList("CAEP");
    }

    @Override
    public void publish(SecurityEventTokenPayload eventPayload, EventContext eventContext)
            throws EventPublisherException {

        makeAsyncAPICall(eventPayload, eventContext);
    }

    @Override
    public boolean canHandleEvent(EventContext eventContext) throws EventPublisherException {

        try {
            final List<Webhook> activeWebhooks = SSFAdapterDataHolder.getInstance().getWebhookManagementService()
                    .getActiveWebhooks(eventContext.getEventProfileName(), eventContext.getEventProfileVersion(),
                            eventContext.getEventUri(), eventContext.getTenantDomain());
            return !activeWebhooks.isEmpty();
        } catch (WebhookMgtException e) {
            throw new EventPublisherServerException(ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getMessage(),
                    ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getDescription(), ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getCode(), e);
        }
    }

    private void makeAsyncAPICall(SecurityEventTokenPayload eventPayload, EventContext eventContext)
            throws EventPublisherServerException {

        final String tenantDomain = eventContext.getTenantDomain();
        final int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        final List<Webhook> activeWebhooks;
        try {
            activeWebhooks = SSFAdapterDataHolder.getInstance().getWebhookManagementService()
                    .getActiveWebhooks(eventContext.getEventProfileName(), eventContext.getEventProfileVersion(),
                            eventContext.getEventUri(), tenantDomain);
        } catch (WebhookMgtException e) {
            throw new EventPublisherServerException(ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getMessage(),
                    ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getDescription(), ERROR_ACTIVE_WEBHOOKS_RETRIEVAL.getCode(), e);
        }

        for (Webhook webhook : activeWebhooks) {
            final String url = webhook.getEndpoint();

            final JWTClaimsSet claimsSet;
            try {
                claimsSet = buildClaimsSet(eventPayload, webhook);
            } catch (ParseException e) {
                log.error("Failed to build claims for webhook: " + webhook.getId() +
                        ". Event will not be published to the endpoint: " + url, e);
                continue;
            }

            sendWithRetries(url, claimsSet, tenantDomain, tenantId,
                    SSFAdapterDataHolder.getInstance().getClientManager().getMaxRetries());
        }
    }

    /**
     * Build the JWT claims for a single webhook, from the shared event payload.
     *
     * TODO: 'aud' is currently a hardcoded mock value for development/testing only.
     * Have to Replace with a real lookup once Stream Management persists the audience
     * a companion DAO call keyed by webhook.getId() (if stored in a separate table).
     */
    private JWTClaimsSet buildClaimsSet(SecurityEventTokenPayload eventPayload, Webhook webhook)
            throws ParseException {

        @SuppressWarnings("unchecked")
        Map<String, Object> claimsMap = MAPPER.convertValue(eventPayload, Map.class);
        claimsMap.put("aud", "mock-audience");
        return JWTClaimsSet.parse(claimsMap);
    }

    private void sendWithRetries(String url, JWTClaimsSet claimsSet, String tenantDomain, int tenantId,
                                 int retriesLeft) {

        ClientManager clientManager = SSFAdapterDataHolder.getInstance().getClientManager();

        final HttpPost request;
        try {
            request = clientManager.createHttpPost(url, claimsSet, tenantDomain);
        } catch (SSFAdapterException e) {
            log.debug("Error constructing signed HTTP request for SSF publish. No retries will be attempted.", e);
            return;
        }

        CompletableFuture<HttpResponse> future = clientManager.executeAsync(request);

        future.whenCompleteAsync((response, throwable) -> {
            try {
                // TODO: restore tenant/correlation context here once MDC/correlation propagation is built —
                // MDC.put(...), PrivilegedCarbonContext.startTenantFlow(). See HTTPEventPublisherImpl's
                // callback for the reference shape; needs the application.authentication.framework dependency.

                if (throwable == null) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        log.debug("SSF event published successfully. Response code: " + status +
                                ", Endpoint: " + url);
                    } else if (status >= 300 && status < 400) {
                        log.warn("Endpoint returned a redirection. Status code: " + status + ". Url: " + url);
                        // No retry for redirection.
                    } else if (status >= 400 && status < 500) {
                        log.warn("Endpoint returned a client error. Status code: " + status + ". Url: " + url);
                        // No retry for client error.
                    } else {
                        log.warn("Received server error from endpoint. Status code: " + status + ". Url: " + url);
                        if (retriesLeft > 0) {
                            sendWithRetries(url, claimsSet, tenantDomain, tenantId, retriesLeft - 1);
                        } else {
                            log.warn("Failed to publish SSF event to endpoint: " + url + ". Maximum retries reached.");
                        }
                    }
                } else {
                    boolean shouldRetry = false;
                    String errorMsg = "Failed to publish SSF event to endpoint. ";
                    if (throwable.getCause() instanceof SocketTimeoutException ||
                            throwable.getCause() instanceof ConnectTimeoutException) {
                        errorMsg += "Request timed out.";
                        shouldRetry = true;
                    } else if (throwable.getCause() instanceof IOException) {
                        errorMsg += "IO error occurred.";
                        shouldRetry = true;
                    } else {
                        errorMsg += "Unexpected error: " + throwable.getMessage();
                    }

                    if (shouldRetry && retriesLeft > 0) {
                        log.warn(errorMsg + " Url: " + url + " Retrying... (" + retriesLeft + " attempts left)");
                        sendWithRetries(url, claimsSet, tenantDomain, tenantId, retriesLeft - 1);
                    } else {
                        log.warn(errorMsg + " Url: " + url);
                        log.debug(errorMsg, throwable);
                    }
                }
            } finally {
                if (response != null && response.getEntity() != null) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                // TODO: clear tenant/correlation context here (PrivilegedCarbonContext.endTenantFlow(), MDC.clear()),
                // matching whatever gets set up in the try block above.

            }
        }, clientManager.getAsyncCallbackExecutor());
    }
}
