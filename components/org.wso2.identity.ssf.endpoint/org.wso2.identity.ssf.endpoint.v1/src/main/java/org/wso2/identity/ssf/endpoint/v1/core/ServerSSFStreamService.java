/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.ssf.endpoint.v1.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.auth.service.AuthenticationContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.identity.ssf.endpoint.v1.model.Delivery;
import org.wso2.identity.ssf.endpoint.v1.model.StatusRequest;
import org.wso2.identity.ssf.endpoint.v1.model.StatusResponse;
import org.wso2.identity.ssf.endpoint.v1.model.StreamList;
import org.wso2.identity.ssf.endpoint.v1.model.StreamRequest;
import org.wso2.identity.ssf.endpoint.v1.model.StreamResponse;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementClientException;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementException;
import org.wso2.identity.ssf.stream.management.api.model.StreamConfiguration;
import org.wso2.identity.ssf.stream.management.api.model.StreamStatus;
import org.wso2.identity.ssf.stream.management.api.service.SSFStreamManagementService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

/**
 * Call internal osgi services to perform SSF stream management operations.
 */
public class ServerSSFStreamService {

    private static final Log LOG = LogFactory.getLog(ServerSSFStreamService.class);

    private final SSFStreamManagementService ssfStreamManagementService;

    public ServerSSFStreamService(SSFStreamManagementService ssfStreamManagementService) {

        this.ssfStreamManagementService = ssfStreamManagementService;
    }

    /**
     * Create stream.
     *
     * @param streamRequest      Stream creation request.
     * @param httpServletRequest The current HTTP request, used to identify the calling application.
     * @return Created stream.
     */
    public StreamResponse createStream(StreamRequest streamRequest, HttpServletRequest httpServletRequest) {

        try {
            StreamConfiguration configuration = buildStreamConfiguration(streamRequest);
            StreamConfiguration created = ssfStreamManagementService.createStream(configuration,
                    getReceiverAudience(httpServletRequest), getTenantDomain());
            return toStreamResponse(created);
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Get a single stream.
     *
     * @param streamId Stream ID.
     * @return The stream.
     */
    public StreamResponse getStream(String streamId) {

        try {
            return toStreamResponse(ssfStreamManagementService.getStream(streamId, getTenantDomain()));
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Get every stream for the tenant.
     *
     * @return The streams.
     */
    public StreamList getStreams() {

        try {
            List<StreamResponse> streams = ssfStreamManagementService.getStreams(getTenantDomain()).stream()
                    .map(this::toStreamResponse)
                    .collect(Collectors.toList());
            return new StreamList().streams(streams);
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Partially update a stream.
     *
     * @param streamRequest      Partial update request, must include streamId.
     * @param httpServletRequest The current HTTP request, used to verify the caller owns this stream.
     * @return The updated stream.
     */
    public StreamResponse updateStream(StreamRequest streamRequest, HttpServletRequest httpServletRequest) {

        try {
            verifyStreamOwnership(streamRequest.getStreamId(), httpServletRequest);
            StreamConfiguration configuration = buildStreamConfiguration(streamRequest);
            StreamConfiguration updated = ssfStreamManagementService.updateStream(configuration, getTenantDomain());
            return toStreamResponse(updated);
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Fully replace a stream.
     *
     * @param streamRequest      Replacement request, must include streamId.
     * @param httpServletRequest The current HTTP request, used to verify the caller owns this stream.
     * @return The replaced stream.
     */
    public StreamResponse replaceStream(StreamRequest streamRequest, HttpServletRequest httpServletRequest) {

        try {
            verifyStreamOwnership(streamRequest.getStreamId(), httpServletRequest);
            StreamConfiguration configuration = buildStreamConfiguration(streamRequest);
            StreamConfiguration replaced = ssfStreamManagementService.replaceStream(configuration, getTenantDomain());
            return toStreamResponse(replaced);
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Delete a stream.
     *
     * @param streamId            Stream ID.
     * @param httpServletRequest  The current HTTP request, used to verify the caller owns this stream.
     */
    public void deleteStream(String streamId, HttpServletRequest httpServletRequest) {

        try {
            verifyStreamOwnership(streamId, httpServletRequest);
            ssfStreamManagementService.deleteStream(streamId, getTenantDomain());
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Get a stream's status.
     *
     * @param streamId Stream ID.
     * @return The stream's status.
     */
    public StatusResponse getStatus(String streamId) {

        try {
            return toStatusResponse(ssfStreamManagementService.getStatus(streamId, getTenantDomain()));
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    /**
     * Update a stream's status.
     *
     * @param statusRequest Status update request, must include streamId and status.
     * @return The updated status.
     */
    public StatusResponse updateStatus(StatusRequest statusRequest) {

        try {
            StreamStatus statusUpdate = new StreamStatus.Builder()
                    .streamId(statusRequest.getStreamId())
                    .status(statusRequest.getStatus() != null ? statusRequest.getStatus().value() : null)
                    .reason(statusRequest.getReason())
                    .build();
            StreamStatus updated = ssfStreamManagementService.updateStatus(statusUpdate, getTenantDomain());
            return toStatusResponse(updated);
        } catch (SSFStreamManagementException e) {
            throw buildAPIError(e);
        }
    }

    private StreamConfiguration buildStreamConfiguration(StreamRequest streamRequest) {

        org.wso2.identity.ssf.stream.management.api.model.Delivery delivery = null;
        if (streamRequest.getDelivery() != null) {
            delivery = new org.wso2.identity.ssf.stream.management.api.model.Delivery.Builder()
                    .method(streamRequest.getDelivery().getMethod())
                    .endpointUrl(streamRequest.getDelivery().getEndpointUrl())
                    .build();
        }
        return new StreamConfiguration.Builder()
                .streamId(streamRequest.getStreamId())
                .delivery(delivery)
                .eventsRequested(streamRequest.getEventsRequested())
                .description(streamRequest.getDescription())
                .build();
    }

    private StreamResponse toStreamResponse(StreamConfiguration configuration) {

        Delivery delivery = null;
        if (configuration.getDelivery() != null) {
            delivery = new Delivery()
                    .method(configuration.getDelivery().getMethod())
                    .endpointUrl(configuration.getDelivery().getEndpointUrl());
        }
        return new StreamResponse()
                .streamId(configuration.getStreamId())
                .iss(configuration.getIss())
                .aud(configuration.getAud())
                .delivery(delivery)
                .eventsSupported(configuration.getEventsSupported())
                .eventsRequested(configuration.getEventsRequested())
                .eventsDelivered(configuration.getEventsDelivered())
                .description(configuration.getDescription());
    }

    private StatusResponse toStatusResponse(StreamStatus status) {

        return new StatusResponse()
                .streamId(status.getStreamId())
                .status(status.getStatus() != null ? StatusResponse.StatusEnum.fromValue(status.getStatus()) : null)
                .reason(status.getReason());
    }

    private String getTenantDomain() {

        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Verify that the caller is one of the receivers this stream was created for, i.e. that the
     * caller's own resolved audience overlaps with the stream's recorded {@code aud}. This stops
     * one application from managing another application's stream even when both hold the
     * {@code ssf.manage} scope.
     *
     * @param streamId           Stream ID.
     * @param httpServletRequest The current HTTP request, used to resolve the caller's audience.
     * @throws SSFStreamManagementException If no stream exists with the given ID.
     */
    private void verifyStreamOwnership(String streamId, HttpServletRequest httpServletRequest)
            throws SSFStreamManagementException {

        StreamConfiguration existing = ssfStreamManagementService.getStream(streamId, getTenantDomain());
        List<String> streamAudience = existing.getAud();
        List<String> callerAudience = getReceiverAudience(httpServletRequest);
        boolean authorized = streamAudience != null && callerAudience != null &&
                streamAudience.stream().anyMatch(callerAudience::contains);
        if (!authorized) {
            throw new WebApplicationException("The caller is not authorized to manage this stream.",
                    Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    /**
     * The receiver's audience - the configured OIDC audience of the OAuth2 application that
     * authenticated the current request. Defaults to that application's own client ID unless a
     * custom audience was configured for it.
     *
     * The consumer key is reached via the {@code AuthenticationContext} that {@code
     * AuthenticationValve} (identity-carbon-auth-rest) attaches to the current request as the
     * "auth-context" attribute - not via {@code IdentityContext}, which doesn't expose it for a
     * plain Bearer-token REST call (its {@code ApplicationActor} only sets {@code entityId},
     * which has no public getter in this version).
     *
     * @param httpServletRequest The current HTTP request.
     * @return The application's configured audience, or an empty list if it can't be determined.
     */
    private List<String> getReceiverAudience(HttpServletRequest httpServletRequest) {

        Object authContextAttribute = httpServletRequest.getAttribute("auth-context");
        if (!(authContextAttribute instanceof AuthenticationContext)) {
            LOG.warn("No AuthenticationContext found on the request; defaulting to an empty audience.");
            return Collections.emptyList();
        }
        AuthenticationContext authenticationContext = (AuthenticationContext) authContextAttribute;

        Object consumerKeyProperty = authenticationContext.getParameter("consumer-key");
        if (!(consumerKeyProperty instanceof String)) {
            LOG.warn("No consumer key found in the AuthenticationContext; defaulting to an empty audience.");
            return Collections.emptyList();
        }
        String consumerKey = (String) consumerKeyProperty;

        try {
            int tenantId = IdentityTenantUtil.getTenantId(getTenantDomain());
            OAuthAppDO oAuthAppDO = new OAuthAppDAO().getAppInformation(consumerKey, tenantId);
            return OAuth2Util.getOIDCAudience(consumerKey, oAuthAppDO);
        } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
            LOG.error("Error while retrieving the configured audience for consumer key: " + consumerKey);
            return Collections.emptyList();
        }
    }

    private WebApplicationException buildAPIError(SSFStreamManagementException e) {

        Response.Status status = e instanceof SSFStreamManagementClientException
                ? Response.Status.BAD_REQUEST
                : Response.Status.INTERNAL_SERVER_ERROR;
        LOG.error("SSF stream management error " + e.getErrorCode() + ": " + e.getMessage() +
                " - " + e.getDescription() + " caused by: " + causeChainToString(e));
        return new WebApplicationException(e.getMessage(),
                Response.status(status).entity(e.getMessage()).build());
    }

    private String causeChainToString(Throwable e) {

        StringBuilder sb = new StringBuilder();
        Throwable cause = e.getCause();
        while (cause != null) {
            sb.append(" -> ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
            cause = cause.getCause();
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }
}
