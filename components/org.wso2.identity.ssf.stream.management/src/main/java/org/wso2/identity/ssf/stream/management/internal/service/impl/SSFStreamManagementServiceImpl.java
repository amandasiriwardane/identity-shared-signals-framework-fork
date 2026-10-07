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

package org.wso2.identity.ssf.stream.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementClientException;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementException;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementServerException;
import org.wso2.identity.ssf.stream.management.api.model.Delivery;
import org.wso2.identity.ssf.stream.management.api.model.StreamConfiguration;
import org.wso2.identity.ssf.stream.management.api.model.StreamStatus;
import org.wso2.identity.ssf.stream.management.api.service.SSFStreamManagementService;
import org.wso2.identity.ssf.stream.management.internal.component.SSFStreamManagementDataHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SSFStreamManagementService. Translates between the SSF spec's external
 * stream configuration shape and the internal {@code Webhook} model.
 */
public class SSFStreamManagementServiceImpl implements SSFStreamManagementService {

    private static final Log LOG = LogFactory.getLog(SSFStreamManagementServiceImpl.class);

    private static final String CAEP_EVENT_PROFILE_NAME = "CAEP";
    private static final String SUPPORTED_DELIVERY_METHOD = "urn:ietf:rfc:8935";
    private static final String AUD_PROPERTY_KEY = "aud";
    private static final String DESCRIPTION_PROPERTY_KEY = "description";
    private static final String WEBHOOK_NAME_PREFIX = "SSF Stream - ";
    private static final String NAME_SANITIZE_REGEX = "[^a-zA-Z0-9\\-_ ]";
    private static final String ENABLED_STATUS = "enabled";
    private static final String DISABLED_STATUS = "disabled";

    @Override
    public StreamConfiguration createStream(StreamConfiguration streamRequest, List<String> receiverAudience,
                                            String tenantDomain) throws SSFStreamManagementException {

        validateDeliveryMethod(streamRequest);
        EventProfile caepProfile = getCaepEventProfile();

        Map<String, Object> properties = new HashMap<>();
        properties.put(AUD_PROPERTY_KEY, receiverAudience);
        if (streamRequest.getDescription() != null) {
            properties.put(DESCRIPTION_PROPERTY_KEY, streamRequest.getDescription());
        }

        Webhook webhookToCreate = new Webhook.Builder()
                .endpoint(streamRequest.getDelivery().getEndpointUrl())
                .name(buildWebhookName(receiverAudience))
                .secret(UUID.randomUUID().toString())
                .eventProfileName(caepProfile.getProfile())
                .eventProfileUri(caepProfile.getUri())
                .status(WebhookStatus.ACTIVE)
                .eventsSubscribed(buildSubscriptions(streamRequest.getEventsRequested(), caepProfile))
                .properties(properties)
                .build();

        Webhook createdWebhook;
        try {
            createdWebhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .createWebhook(webhookToCreate, tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65001",
                    "Error while creating the stream.", e.getMessage(), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Created SSF stream with id: " + createdWebhook.getId() + " for tenant: " + tenantDomain);
        }

        return toStreamConfiguration(createdWebhook, caepProfile, tenantDomain);
    }

    private void validateDeliveryMethod(StreamConfiguration streamRequest) throws SSFStreamManagementException {

        if (streamRequest.getDelivery() == null
                || !SUPPORTED_DELIVERY_METHOD.equals(streamRequest.getDelivery().getMethod())) {
            throw new SSFStreamManagementClientException("SSFSTREAM-60001",
                    "Unsupported delivery method.",
                    "Only push delivery (" + SUPPORTED_DELIVERY_METHOD + ") is supported.");
        }
    }

    private EventProfile getCaepEventProfile() throws SSFStreamManagementException {

        List<EventProfile> eventProfiles;
        try {
            eventProfiles = SSFStreamManagementDataHolder.getInstance().getWebhookMetadataService()
                    .getSupportedEventProfiles();
        } catch (WebhookMetadataException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65002",
                    "Error while retrieving the CAEP event profile.", e.getMessage(), e);
        }
        return eventProfiles.stream()
                .filter(profile -> CAEP_EVENT_PROFILE_NAME.equalsIgnoreCase(profile.getProfile()))
                .findFirst()
                .orElseThrow(() -> new SSFStreamManagementServerException("SSFSTREAM-65003",
                        "CAEP event profile is not registered.",
                        "No event profile named " + CAEP_EVENT_PROFILE_NAME + " was found."));
    }

    private List<Subscription> buildSubscriptions(List<String> eventsRequested, EventProfile caepProfile)
            throws SSFStreamManagementException {

        if (eventsRequested == null) {
            return Collections.emptyList();
        }
        List<Subscription> subscriptions = new ArrayList<>();
        for (String eventUri : eventsRequested) {
            subscriptions.add(Subscription.builder()
                    .channelUri(resolveChannelUriForEvent(caepProfile, eventUri))
                    .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                    .build());
        }
        return subscriptions;
    }

    /**
     * Find the channel that delivers the given event type, as requested by a caller using the
     * SSF spec's own event-type URIs (e.g. ".../event-type/session-revoked") - which are distinct
     * from the channel URIs the underlying webhook subscription model is keyed on.
     *
     * @param caepProfile CAEP event profile.
     * @param eventUri    Event type URI, as sent by the caller.
     * @return The URI of the channel that delivers this event type.
     * @throws SSFStreamManagementException If no channel delivers the given event type.
     */
    private String resolveChannelUriForEvent(EventProfile caepProfile, String eventUri)
            throws SSFStreamManagementException {

        for (Channel channel : caepProfile.getChannels()) {
            for (Event event : channel.getEvents()) {
                if (event.getEventUri().equalsIgnoreCase(eventUri)) {
                    return channel.getUri();
                }
            }
        }
        throw new SSFStreamManagementClientException("SSFSTREAM-60003",
                "Unsupported event type.", "No channel delivers the event type: " + eventUri);
    }

    /**
     * The reverse lookup of {@link #resolveChannelUriForEvent} - given a channel URI (as stored
     * on a {@code Subscription}), return the event-type URIs of every event that channel delivers.
     *
     * @param caepProfile CAEP event profile.
     * @param channelUri  Channel URI, as stored on a {@code Subscription}.
     * @return The event type URIs delivered by that channel, or an empty list if the channel is
     *         unrecognized.
     */
    private List<String> resolveEventUrisForChannel(EventProfile caepProfile, String channelUri) {

        for (Channel channel : caepProfile.getChannels()) {
            if (channel.getUri().equalsIgnoreCase(channelUri)) {
                return channel.getEvents().stream()
                        .map(Event::getEventUri)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private String buildWebhookName(List<String> receiverAudience) {

        String audience = (receiverAudience != null && !receiverAudience.isEmpty())
                ? receiverAudience.get(0) : "unknown";
        String sanitized = audience.replaceAll(NAME_SANITIZE_REGEX, "-");
        return (WEBHOOK_NAME_PREFIX + sanitized).trim();
    }

    private StreamConfiguration toStreamConfiguration(Webhook webhook, EventProfile caepProfile,
                                                       String tenantDomain) throws SSFStreamManagementException {

        String iss;
        try {
            iss = ServiceURLBuilder.create().setTenant(tenantDomain).build().getAbsolutePublicUrlWithoutPath();
        } catch (URLBuilderException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65004",
                    "Error while building the issuer URL.", e.getMessage(), e);
        }

        List<String> eventsSupported = caepProfile.getChannels().stream()
                .flatMap(channel -> channel.getEvents().stream())
                .map(Event::getEventUri)
                .collect(Collectors.toList());

        List<String> eventsRequested = new ArrayList<>();
        try {
            for (Subscription subscription : webhook.getEventsSubscribed()) {
                eventsRequested.addAll(resolveEventUrisForChannel(caepProfile, subscription.getChannelUri()));
            }
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65005",
                    "Error while retrieving the stream's subscribed events.", e.getMessage(), e);
        }

        List<String> eventsDelivered = eventsRequested.stream()
                .filter(eventsSupported::contains)
                .collect(Collectors.toList());

        List<String> aud = toAudList(webhook.getProperties().get(AUD_PROPERTY_KEY));
        Object descriptionProperty = webhook.getProperties().get(DESCRIPTION_PROPERTY_KEY);

        return new StreamConfiguration.Builder()
                .streamId(webhook.getId())
                .iss(iss)
                .aud(aud)
                .delivery(new Delivery.Builder()
                        .method(SUPPORTED_DELIVERY_METHOD)
                        .endpointUrl(webhook.getEndpoint())
                        .build())
                .eventsSupported(eventsSupported)
                .eventsRequested(eventsRequested)
                .eventsDelivered(eventsDelivered)
                .description(descriptionProperty != null ? descriptionProperty.toString() : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> toAudList(Object audProperty) {

        if (audProperty instanceof List) {
            return (List<String>) audProperty;
        }
        if (audProperty instanceof String) {
            return Collections.singletonList((String) audProperty);
        }
        return Collections.emptyList();
    }

    @Override
    public StreamConfiguration getStream(String streamId, String tenantDomain) throws SSFStreamManagementException {

        Webhook webhook = getStreamWebhook(streamId, tenantDomain);
        EventProfile caepProfile = getCaepEventProfile();
        return toStreamConfiguration(webhook, caepProfile, tenantDomain);
    }

    /**
     * Fetch the webhook backing a stream, verifying it exists and is actually CAEP-profiled -
     * a streamId is just a webhook UUID, and UUIDs are unique across all webhooks regardless of
     * profile, so a caller could otherwise pass in the id of an unrelated, non-CAEP webhook.
     *
     * @param streamId     Stream ID.
     * @param tenantDomain Tenant domain.
     * @return The backing webhook.
     * @throws SSFStreamManagementException If no such CAEP stream exists, or retrieval fails.
     */
    private Webhook getStreamWebhook(String streamId, String tenantDomain) throws SSFStreamManagementException {

        Webhook webhook;
        try {
            webhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .getWebhook(streamId, tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65006",
                    "Error while retrieving the stream.", e.getMessage(), e);
        }
        if (webhook == null || !CAEP_EVENT_PROFILE_NAME.equalsIgnoreCase(webhook.getEventProfileName())) {
            throw new SSFStreamManagementClientException("SSFSTREAM-60002",
                    "Stream not found.", "No stream exists with id: " + streamId);
        }
        return webhook;
    }

    @Override
    public List<StreamConfiguration> getStreams(String tenantDomain) throws SSFStreamManagementException {

        List<Webhook> webhooks;
        try {
            webhooks = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .getWebhooks(tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65007",
                    "Error while retrieving streams.", e.getMessage(), e);
        }

        EventProfile caepProfile = getCaepEventProfile();
        List<StreamConfiguration> streams = new ArrayList<>();
        for (Webhook webhook : webhooks) {
            if (CAEP_EVENT_PROFILE_NAME.equalsIgnoreCase(webhook.getEventProfileName())) {
                streams.add(toStreamConfiguration(webhook, caepProfile, tenantDomain));
            }
        }
        return streams;
    }

    @Override
    public StreamConfiguration updateStream(StreamConfiguration streamUpdate, String tenantDomain)
            throws SSFStreamManagementException {

        Webhook existing = getStreamWebhook(streamUpdate.getStreamId(), tenantDomain);
        EventProfile caepProfile = getCaepEventProfile();

        String endpoint = existing.getEndpoint();
        if (streamUpdate.getDelivery() != null) {
            validateDeliveryMethod(streamUpdate);
            endpoint = streamUpdate.getDelivery().getEndpointUrl();
        }

        List<Subscription> eventsSubscribed;
        if (streamUpdate.getEventsRequested() != null && !streamUpdate.getEventsRequested().isEmpty()) {
            eventsSubscribed = buildSubscriptions(streamUpdate.getEventsRequested(), caepProfile);
        } else {
            try {
                eventsSubscribed = existing.getEventsSubscribed();
            } catch (WebhookMgtException e) {
                throw new SSFStreamManagementServerException("SSFSTREAM-65009",
                        "Error while retrieving the stream's subscribed events.", e.getMessage(), e);
            }
        }

        Map<String, Object> properties = new HashMap<>(existing.getProperties());
        if (streamUpdate.getDescription() != null) {
            properties.put(DESCRIPTION_PROPERTY_KEY, streamUpdate.getDescription());
        }

        Webhook mergedWebhook = new Webhook.Builder()
                .uuid(existing.getId())
                .endpoint(endpoint)
                .name(existing.getName())
                .secret(existing.getSecret())
                .eventProfileName(existing.getEventProfileName())
                .eventProfileUri(existing.getEventProfileUri())
                .status(existing.getStatus())
                .eventsSubscribed(eventsSubscribed)
                .properties(properties)
                .build();

        Webhook updatedWebhook;
        try {
            updatedWebhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .updateWebhook(existing.getId(), mergedWebhook, tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65010",
                    "Error while updating the stream.", e.getMessage(), e);
        }

        return toStreamConfiguration(updatedWebhook, caepProfile, tenantDomain);
    }

    @Override
    public StreamConfiguration replaceStream(StreamConfiguration streamReplacement, String tenantDomain)
            throws SSFStreamManagementException {

        Webhook existing = getStreamWebhook(streamReplacement.getStreamId(), tenantDomain);
        validateDeliveryMethod(streamReplacement);
        EventProfile caepProfile = getCaepEventProfile();

        Map<String, Object> properties = new HashMap<>(existing.getProperties());
        if (streamReplacement.getDescription() != null) {
            properties.put(DESCRIPTION_PROPERTY_KEY, streamReplacement.getDescription());
        } else {
            properties.remove(DESCRIPTION_PROPERTY_KEY);
        }

        Webhook replacementWebhook = new Webhook.Builder()
                .uuid(existing.getId())
                .endpoint(streamReplacement.getDelivery().getEndpointUrl())
                .name(existing.getName())
                .secret(existing.getSecret())
                .eventProfileName(existing.getEventProfileName())
                .eventProfileUri(existing.getEventProfileUri())
                .status(existing.getStatus())
                .eventsSubscribed(buildSubscriptions(streamReplacement.getEventsRequested(), caepProfile))
                .properties(properties)
                .build();

        Webhook updatedWebhook;
        try {
            updatedWebhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .updateWebhook(existing.getId(), replacementWebhook, tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65011",
                    "Error while replacing the stream.", e.getMessage(), e);
        }

        return toStreamConfiguration(updatedWebhook, caepProfile, tenantDomain);
    }

    @Override
    public void deleteStream(String streamId, String tenantDomain) throws SSFStreamManagementException {

        getStreamWebhook(streamId, tenantDomain);
        try {
            SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                    .deleteWebhook(streamId, tenantDomain);
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65008",
                    "Error while deleting the stream.", e.getMessage(), e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted SSF stream with id: " + streamId + " for tenant: " + tenantDomain);
        }
    }

    @Override
    public StreamStatus getStatus(String streamId, String tenantDomain) throws SSFStreamManagementException {

        Webhook webhook = getStreamWebhook(streamId, tenantDomain);
        return new StreamStatus.Builder()
                .streamId(streamId)
                .status(toSSFStatus(webhook.getStatus()))
                .build();
    }

    private String toSSFStatus(WebhookStatus webhookStatus) {

        if (webhookStatus == WebhookStatus.ACTIVE || webhookStatus == WebhookStatus.PARTIALLY_ACTIVE) {
            return ENABLED_STATUS;
        }
        return DISABLED_STATUS;
    }

    @Override
    public StreamStatus updateStatus(StreamStatus statusUpdate, String tenantDomain)
            throws SSFStreamManagementException {

        String requestedStatus = statusUpdate.getStatus();
        boolean enable = ENABLED_STATUS.equalsIgnoreCase(requestedStatus);
        if (!enable && !DISABLED_STATUS.equalsIgnoreCase(requestedStatus)) {
            throw new SSFStreamManagementClientException("SSFSTREAM-60004",
                    "Unsupported stream status.",
                    "Only \"" + ENABLED_STATUS + "\" and \"" + DISABLED_STATUS + "\" are supported.");
        }

        getStreamWebhook(statusUpdate.getStreamId(), tenantDomain);

        Webhook updatedWebhook;
        try {
            if (enable) {
                updatedWebhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                        .activateWebhook(statusUpdate.getStreamId(), tenantDomain);
            } else {
                updatedWebhook = SSFStreamManagementDataHolder.getInstance().getWebhookManagementService()
                        .deactivateWebhook(statusUpdate.getStreamId(), tenantDomain);
            }
        } catch (WebhookMgtException e) {
            throw new SSFStreamManagementServerException("SSFSTREAM-65012",
                    "Error while updating the stream's status.", e.getMessage(), e);
        }

        return new StreamStatus.Builder()
                .streamId(statusUpdate.getStreamId())
                .status(toSSFStatus(updatedWebhook.getStatus()))
                .reason(statusUpdate.getReason())
                .build();
    }
}
