package org.wso2.identity.event.ssf.publisher.internal.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherServerException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterException;
import org.wso2.identity.event.ssf.publisher.internal.component.ClientManager;
import org.wso2.identity.event.ssf.publisher.internal.component.SSFAdapterDataHolder;
import org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants;
import org.wso2.identity.event.ssf.publisher.internal.util.SSFAdapterUtil;
import org.wso2.identity.event.ssf.publisher.internal.util.SSFCorrelationLogUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.wso2.carbon.CarbonConstants.LogEventConstants.TENANT_ID;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.CORRELATION_ID_MDC;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.TENANT_DOMAIN;
import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_ACTIVE_WEBHOOKS_RETRIEVAL;
import static org.wso2.identity.event.ssf.publisher.internal.util.SSFAdapterUtil.printPublisherDiagnosticLog;
import static org.wso2.identity.event.ssf.publisher.internal.util.SSFCorrelationLogUtils.handleResponseCorrelationLog;

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

        final String correlationId = SSFAdapterUtil.getCorrelationID(eventPayload);
        final String tenantDomain = eventContext.getTenantDomain();
        final int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        final String eventProfileName = eventContext.getEventProfileName();
        final String eventProfileUri = eventContext.getEventUri();
        final String events = String.join(",", eventPayload.getEvents().keySet());

        final Map<String, String> copiedMDCSnapshot =
                MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : Collections.emptyMap();


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
                printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                        SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT, DiagnosticLog.ResultStatus.FAILED,
                        "Failed to build claims for webhook. Event will not be published to the endpoint: " + url);
            continue;
            }

            sendWithRetries(eventProfileName, eventProfileUri, events, copiedMDCSnapshot,
                    correlationId, tenantDomain, tenantId, url, claimsSet,
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

    private void sendWithRetries(String eventProfileName, String eventProfileUri, String events,
                                Map<String, String> mdcSnapshot, String correlationId, String tenantDomain,
                                int tenantId, String url, JWTClaimsSet claimsSet, int retriesLeft) {

        ClientManager clientManager = SSFAdapterDataHolder.getInstance().getClientManager();

        final HttpPost request;
        try {
            request = clientManager.createHttpPost(url, claimsSet, tenantDomain);
        } catch (SSFAdapterException e) {
            printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                    SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT, DiagnosticLog.ResultStatus.FAILED,
                    "Failed to construct signed HTTP request for SSF publish.");
            log.debug("Error constructing signed HTTP request for SSF publish. No retries will be attempted.", e);
            return;
        }

        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT, DiagnosticLog.ResultStatus.SUCCESS,
                "Publishing event data to endpoint.");

        final long requestStartTime = System.currentTimeMillis();

        CompletableFuture<HttpResponse> future = clientManager.executeAsync(request);

        future.whenCompleteAsync((response, throwable) -> {
            try {
                MDC.clear();
                if (mdcSnapshot != null && !mdcSnapshot.isEmpty()) {
                    MDC.setContextMap(mdcSnapshot);
                }
                if (StringUtils.isNotBlank(correlationId)) {
                    MDC.put(CORRELATION_ID_MDC, correlationId);
                }
                MDC.put(TENANT_DOMAIN, tenantDomain);
                MDC.put(TENANT_ID, String.valueOf(tenantId));
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

                if (throwable == null) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        handleResponseCorrelationLog(request, requestStartTime,
                                SSFCorrelationLogUtils.RequestStatus.COMPLETED.getStatus(),
                                String.valueOf(status), response.getStatusLine().getReasonPhrase());
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.SUCCESS, "Event data published to endpoint.");
                        log.debug("SSF event published successfully. Response code: " + status +
                                ", Endpoint: " + url);
                    } else if (status >= 300 && status < 400) {
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.FAILED,
                                "Endpoint returned a redirection. Status code: " + status);
                        log.warn("Endpoint returned a redirection. Status code: " + status + ". Url: " + url);
                        // No retry for redirection.
                    } else if (status >= 400 && status < 500) {
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.FAILED,
                                "Endpoint returned a client error. Status code: " + status);
                        log.warn("Endpoint returned a client error. Status code: " + status + ". Url: " + url);
                        // No retry for client error.
                    } else {
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.FAILED,
                                "Received server error from endpoint. Status code: " + status +
                                        ". Retrying... (" + retriesLeft + " attempts left)");
                        log.warn("Received server error from endpoint. Status code: " + status + ". Url: " + url);
                        if (retriesLeft > 0) {
                            sendWithRetries(eventProfileName, eventProfileUri, events, mdcSnapshot, correlationId, tenantDomain,
                                    tenantId, url, claimsSet, retriesLeft - 1);
                        } else {
                            handleResponseCorrelationLog(request, requestStartTime,
                                    SSFCorrelationLogUtils.RequestStatus.FAILED.getStatus(),
                                    String.valueOf(status), response.getStatusLine().getReasonPhrase());
                            printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                    SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                    DiagnosticLog.ResultStatus.FAILED,
                                    "Failed to publish event data to endpoint. Status code: " + status +
                                            ". Maximum retries reached.");
                            log.warn("Failed to publish SSF event to endpoint: " + url + ". Maximum retries reached.");
                        }
                    }
                } else {
                    // Exception handling and retry for timeouts and IO errors
                    boolean shouldRetry = false;
                    String errorMsg = "Failed to publish SSF event to endpoint. ";
                    if (throwable.getCause() instanceof SocketTimeoutException ||
                            throwable.getCause() instanceof ConnectTimeoutException) {
                        errorMsg += "Request timed out.";
                        shouldRetry = true;
                    } else if (throwable.getCause() instanceof IOException) {
                        errorMsg += "IO error occurred.";
                        shouldRetry = true;
                    } else if (throwable.getCause() instanceof IllegalArgumentException){
                        errorMsg += "Invalid request.";
                    } else {
                        errorMsg += "Unexpected error: " + throwable.getMessage();
                    }

                    if (shouldRetry && retriesLeft > 0) {
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.FAILED,
                                errorMsg + " Retrying... (" + retriesLeft + " attempts left)");
                        log.warn(errorMsg + " Url: " + url + " Retrying... (" + retriesLeft + " attempts left)");
                        sendWithRetries(eventProfileName, eventProfileUri, events, mdcSnapshot, correlationId, tenantDomain,
                                tenantId, url, claimsSet, retriesLeft - 1);
                    } else {
                        errorMsg = errorMsg + (shouldRetry ? " Maximum retries reached." : "");
                        handleResponseCorrelationLog(request, requestStartTime,
                                SSFCorrelationLogUtils.RequestStatus.FAILED.getStatus(), throwable.getMessage());
                        printPublisherDiagnosticLog(eventProfileName, eventProfileUri, events, url,
                                SSFAdapterConstants.LogConstants.ActionIDs.PUBLISH_EVENT,
                                DiagnosticLog.ResultStatus.FAILED, errorMsg);
                        log.warn(errorMsg + " Url: " + url);
                        log.debug(errorMsg, throwable);
                    }
                }
            } finally {
                if (response != null && response.getEntity() != null) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                if (StringUtils.isNotEmpty(correlationId)) {
                    MDC.remove(CORRELATION_ID_MDC);
                }
                MDC.remove(TENANT_DOMAIN);
                MDC.remove(TENANT_ID);
                PrivilegedCarbonContext.endTenantFlow();
                MDC.clear();
            }
        }, clientManager.getAsyncCallbackExecutor());
    }
}
