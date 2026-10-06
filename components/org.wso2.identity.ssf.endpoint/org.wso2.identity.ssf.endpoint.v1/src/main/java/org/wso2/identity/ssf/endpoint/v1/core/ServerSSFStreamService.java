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
import org.wso2.carbon.identity.core.context.IdentityContext;
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
     * @param streamRequest Stream creation request.
     * @return Created stream.
     */
    public StreamResponse createStream(StreamRequest streamRequest) {

        try {
            StreamConfiguration configuration = buildStreamConfiguration(streamRequest);
            StreamConfiguration created = ssfStreamManagementService.createStream(configuration,
                    getReceiverAudience(), getTenantDomain());
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
     * @param streamRequest Partial update request, must include streamId.
     * @return The updated stream.
     */
    public StreamResponse updateStream(StreamRequest streamRequest) {

        try {
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
     * @param streamRequest Replacement request, must include streamId.
     * @return The replaced stream.
     */
    public StreamResponse replaceStream(StreamRequest streamRequest) {

        try {
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
     * @param streamId Stream ID.
     */
    public void deleteStream(String streamId) {

        try {
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
     * The receiver's audience, taken from the {@code aud} claim of its validated access token.
     *
     * TODO: not yet wired up - need to confirm how an access token's claims are reached from a
     * plain JAX-RS resource in this codebase (no existing identity-api-server endpoint does this
     * today). Returns an empty audience until that mechanism is confirmed.
     */
    private List<String> getReceiverAudience() {

        IdentityContext identityContext = IdentityContext.getThreadLocalIdentityContext();
        if (identityContext.isApplicationActor()) {
            LOG.info("DIAGNOSTIC: calling applicationId = " +
                    identityContext.getApplicationActor().getApplicationId());
        } else {
            LOG.info("DIAGNOSTIC: actor is not an application actor.");
        }
        return Collections.emptyList();
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
