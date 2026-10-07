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

package org.wso2.identity.ssf.stream.management;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.identity.ssf.stream.management.api.exception.SSFStreamManagementClientException;
import org.wso2.identity.ssf.stream.management.api.model.Delivery;
import org.wso2.identity.ssf.stream.management.api.model.StreamConfiguration;
import org.wso2.identity.ssf.stream.management.api.model.StreamStatus;
import org.wso2.identity.ssf.stream.management.internal.component.SSFStreamManagementDataHolder;
import org.wso2.identity.ssf.stream.management.internal.service.impl.SSFStreamManagementServiceImpl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for SSFStreamManagementServiceImpl.
 */
public class SSFStreamManagementServiceImplTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_ISSUER_URL = "https://localhost:9443";
    private static final String CAEP_CHANNEL_URI = "urn:example:secevent:caep:session-revoked";
    private static final String CAEP_EVENT_URI = "urn:example:secevent:caep:event-type:session-revoked";

    private WebhookManagementService mockWebhookManagementService;
    private WebhookMetadataService mockWebhookMetadataService;
    private MockedStatic<ServiceURLBuilder> mockedServiceURLBuilder;

    private SSFStreamManagementServiceImpl service;

    @BeforeMethod
    public void setUp() throws Exception {

        mockWebhookManagementService = mock(WebhookManagementService.class);
        mockWebhookMetadataService = mock(WebhookMetadataService.class);
        SSFStreamManagementDataHolder.getInstance().setWebhookManagementService(mockWebhookManagementService);
        SSFStreamManagementDataHolder.getInstance().setWebhookMetadataService(mockWebhookMetadataService);

        // ServiceURLBuilder's real (default) implementation needs a fully wired carbon server
        // context (ConfigurationContextService etc.) that isn't available in a plain unit test,
        // so it's mocked statically here rather than left to run for real.
        ServiceURLBuilder builderMock = mock(ServiceURLBuilder.class);
        ServiceURL serviceURLMock = mock(ServiceURL.class);
        mockedServiceURLBuilder = mockStatic(ServiceURLBuilder.class);
        mockedServiceURLBuilder.when(ServiceURLBuilder::create).thenReturn(builderMock);
        when(builderMock.setTenant(anyString())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(serviceURLMock);
        when(serviceURLMock.getAbsolutePublicUrlWithoutPath()).thenReturn(TEST_ISSUER_URL);

        EventProfile caepProfile = new EventProfile("CAEP", "https://schemas.openid.net/secevent/caep",
                Collections.singletonList(
                        new Channel("Session", "Session events", CAEP_CHANNEL_URI,
                                Collections.singletonList(
                                        new Event("Session revoked", "Notify if a session is revoked",
                                                CAEP_EVENT_URI)))));
        when(mockWebhookMetadataService.getSupportedEventProfiles())
                .thenReturn(Collections.singletonList(caepProfile));

        service = new SSFStreamManagementServiceImpl();
    }

    @AfterMethod
    public void tearDown() {

        mockedServiceURLBuilder.close();
    }

    @Test
    public void testCreateStream() throws Exception {

        StreamConfiguration request = new StreamConfiguration.Builder()
                .delivery(new Delivery.Builder()
                        .method("urn:ietf:rfc:8935")
                        .endpointUrl("https://receiver.example.com/events")
                        .build())
                .eventsRequested(Collections.singletonList(CAEP_EVENT_URI))
                .description("My stream")
                .build();
        List<String> receiverAudience = Arrays.asList("client-id-123", "https://receiver.example.com/web");

        Webhook createdWebhook = buildWebhook("stream-uuid-1", receiverAudience, "My stream",
                Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.createWebhook(any(), anyString())).thenReturn(createdWebhook);

        StreamConfiguration result = service.createStream(request, receiverAudience, TENANT_DOMAIN);

        assertEquals(result.getStreamId(), "stream-uuid-1");
        assertEquals(result.getIss(), TEST_ISSUER_URL);
        assertEquals(result.getAud(), receiverAudience);
        assertTrue(result.getAud() instanceof List, "aud should come back as a real List, not a flattened string");
        assertEquals(result.getDelivery().getEndpointUrl(), "https://receiver.example.com/events");
        assertEquals(result.getDelivery().getMethod(), "urn:ietf:rfc:8935");
        assertEquals(result.getEventsRequested(), Collections.singletonList(CAEP_EVENT_URI));
        assertEquals(result.getEventsSupported(), Collections.singletonList(CAEP_EVENT_URI));
        assertEquals(result.getEventsDelivered(), Collections.singletonList(CAEP_EVENT_URI));
        assertEquals(result.getDescription(), "My stream");
    }

    @Test
    public void testGetStream() throws Exception {

        List<String> aud = Collections.singletonList("client-id-123");
        Webhook existing = buildWebhook("stream-uuid-2", aud, "Existing stream",
                Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-2", TENANT_DOMAIN)).thenReturn(existing);

        StreamConfiguration result = service.getStream("stream-uuid-2", TENANT_DOMAIN);

        assertEquals(result.getStreamId(), "stream-uuid-2");
        assertEquals(result.getAud(), aud);
        assertEquals(result.getDescription(), "Existing stream");
    }

    @Test(expectedExceptions = SSFStreamManagementClientException.class,
            expectedExceptionsMessageRegExp = "Stream not found.")
    public void testGetStreamNotFound() throws Exception {

        when(mockWebhookManagementService.getWebhook("missing-id", TENANT_DOMAIN)).thenReturn(null);

        service.getStream("missing-id", TENANT_DOMAIN);
    }

    @Test
    public void testGetStreamsFiltersOutNonCaepWebhooks() throws Exception {

        Webhook caepWebhook = buildWebhook("caep-stream-id", Collections.singletonList("client-id-123"),
                "CAEP stream", Collections.singletonList(CAEP_CHANNEL_URI));
        Webhook wso2Webhook = new Webhook.Builder()
                .uuid("wso2-webhook-id")
                .endpoint("https://example.com/webhook")
                .name("Plain webhook")
                .secret("secret")
                .eventProfileName("WSO2")
                .eventProfileUri("https://schemas.identity.wso2.org/events")
                .status(WebhookStatus.ACTIVE)
                .eventsSubscribed(Collections.emptyList())
                .properties(Collections.emptyMap())
                .build();
        when(mockWebhookManagementService.getWebhooks(TENANT_DOMAIN))
                .thenReturn(Arrays.asList(caepWebhook, wso2Webhook));

        List<StreamConfiguration> result = service.getStreams(TENANT_DOMAIN);

        assertEquals(result.size(), 1, "the WSO2-profile webhook should be filtered out");
        assertEquals(result.get(0).getStreamId(), "caep-stream-id");
    }

    @Test(expectedExceptions = SSFStreamManagementClientException.class,
            expectedExceptionsMessageRegExp = "Stream not found.")
    public void testGetStreamWrongProfile() throws Exception {

        Webhook wso2Webhook = new Webhook.Builder()
                .uuid("wso2-webhook-id")
                .endpoint("https://example.com/webhook")
                .name("Plain webhook")
                .secret("secret")
                .eventProfileName("WSO2")
                .eventProfileUri("https://schemas.identity.wso2.org/events")
                .status(WebhookStatus.ACTIVE)
                .eventsSubscribed(Collections.emptyList())
                .properties(Collections.emptyMap())
                .build();
        when(mockWebhookManagementService.getWebhook("wso2-webhook-id", TENANT_DOMAIN)).thenReturn(wso2Webhook);

        service.getStream("wso2-webhook-id", TENANT_DOMAIN);
    }

    @Test
    public void testUpdateStreamPreservesAudAndMergesDescriptionOnly() throws Exception {

        List<String> aud = Collections.singletonList("client-id-123");
        Webhook existing = buildWebhook("stream-uuid-3", aud, "Old description",
                Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-3", TENANT_DOMAIN)).thenReturn(existing);
        when(mockWebhookManagementService.updateWebhook(anyString(), any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StreamConfiguration patch = new StreamConfiguration.Builder()
                .streamId("stream-uuid-3")
                .description("New description")
                .build();

        StreamConfiguration result = service.updateStream(patch, TENANT_DOMAIN);

        assertEquals(result.getAud(), aud, "aud must survive an update that doesn't touch it");
        assertEquals(result.getDescription(), "New description");
        assertEquals(result.getDelivery().getEndpointUrl(), "https://receiver.example.com/events",
                "endpoint should stay unchanged since the patch didn't send a new delivery");
        assertEquals(result.getEventsRequested(), Collections.singletonList(CAEP_EVENT_URI),
                "events should stay unchanged since the patch didn't send eventsRequested");
    }

    @Test
    public void testReplaceStreamClearsUnsetDescriptionButKeepsAud() throws Exception {

        List<String> aud = Collections.singletonList("client-id-123");
        Webhook existing = buildWebhook("stream-uuid-4", aud, "Old description",
                Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-4", TENANT_DOMAIN)).thenReturn(existing);
        when(mockWebhookManagementService.updateWebhook(anyString(), any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StreamConfiguration replacement = new StreamConfiguration.Builder()
                .streamId("stream-uuid-4")
                .delivery(new Delivery.Builder()
                        .method("urn:ietf:rfc:8935")
                        .endpointUrl("https://new-receiver.example.com/events")
                        .build())
                .eventsRequested(Collections.singletonList(CAEP_EVENT_URI))
                .build();

        StreamConfiguration result = service.replaceStream(replacement, TENANT_DOMAIN);

        assertEquals(result.getAud(), aud, "aud must never be regenerated/lost on a full replace");
        assertNull(result.getDescription(), "description must be cleared since the PUT request didn't include it");
        assertEquals(result.getDelivery().getEndpointUrl(), "https://new-receiver.example.com/events");
    }

    @Test
    public void testDeleteStream() throws Exception {

        Webhook existing = buildWebhook("stream-uuid-5", Collections.singletonList("client-id-123"),
                "To delete", Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-5", TENANT_DOMAIN)).thenReturn(existing);

        service.deleteStream("stream-uuid-5", TENANT_DOMAIN);

        verify(mockWebhookManagementService).deleteWebhook("stream-uuid-5", TENANT_DOMAIN);
    }

    @Test(expectedExceptions = SSFStreamManagementClientException.class,
            expectedExceptionsMessageRegExp = "Stream not found.")
    public void testDeleteStreamWrongProfileNeverDeletes() throws Exception {

        Webhook wso2Webhook = new Webhook.Builder()
                .uuid("wso2-webhook-id-2")
                .endpoint("https://example.com/webhook")
                .name("Plain webhook")
                .secret("secret")
                .eventProfileName("WSO2")
                .eventProfileUri("https://schemas.identity.wso2.org/events")
                .status(WebhookStatus.ACTIVE)
                .eventsSubscribed(Collections.emptyList())
                .properties(Collections.emptyMap())
                .build();
        when(mockWebhookManagementService.getWebhook("wso2-webhook-id-2", TENANT_DOMAIN)).thenReturn(wso2Webhook);

        try {
            service.deleteStream("wso2-webhook-id-2", TENANT_DOMAIN);
        } finally {
            verify(mockWebhookManagementService, never()).deleteWebhook(anyString(), anyString());
        }
    }

    @Test
    public void testGetStatusEnabled() throws Exception {

        Webhook existing = buildWebhook("stream-uuid-6", Collections.singletonList("client-id-123"),
                null, Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-6", TENANT_DOMAIN)).thenReturn(existing);

        StreamStatus result = service.getStatus("stream-uuid-6", TENANT_DOMAIN);

        assertEquals(result.getStatus(), "enabled");
    }

    @Test
    public void testGetStatusDisabled() throws Exception {

        Webhook existing = new Webhook.Builder()
                .uuid("stream-uuid-7")
                .endpoint("https://receiver.example.com/events")
                .name("SSF Stream - test")
                .secret("secret")
                .eventProfileName("CAEP")
                .eventProfileUri("https://schemas.openid.net/secevent/caep")
                .status(WebhookStatus.INACTIVE)
                .eventsSubscribed(Collections.emptyList())
                .properties(Collections.emptyMap())
                .build();
        when(mockWebhookManagementService.getWebhook("stream-uuid-7", TENANT_DOMAIN)).thenReturn(existing);

        StreamStatus result = service.getStatus("stream-uuid-7", TENANT_DOMAIN);

        assertEquals(result.getStatus(), "disabled");
    }

    @Test
    public void testUpdateStatusEnable() throws Exception {

        Webhook existing = buildWebhook("stream-uuid-8", Collections.singletonList("client-id-123"),
                null, Collections.singletonList(CAEP_CHANNEL_URI));
        when(mockWebhookManagementService.getWebhook("stream-uuid-8", TENANT_DOMAIN)).thenReturn(existing);
        when(mockWebhookManagementService.activateWebhook("stream-uuid-8", TENANT_DOMAIN)).thenReturn(existing);

        StreamStatus statusUpdate = new StreamStatus.Builder()
                .streamId("stream-uuid-8")
                .status("enabled")
                .reason("turning it back on")
                .build();

        StreamStatus result = service.updateStatus(statusUpdate, TENANT_DOMAIN);

        verify(mockWebhookManagementService).activateWebhook("stream-uuid-8", TENANT_DOMAIN);
        assertEquals(result.getStatus(), "enabled");
        assertEquals(result.getReason(), "turning it back on");
    }

    @Test
    public void testUpdateStatusDisable() throws Exception {

        Webhook existing = buildWebhook("stream-uuid-9", Collections.singletonList("client-id-123"),
                null, Collections.singletonList(CAEP_CHANNEL_URI));
        Webhook deactivated = new Webhook.Builder()
                .uuid(existing.getId())
                .endpoint(existing.getEndpoint())
                .name(existing.getName())
                .secret(existing.getSecret())
                .eventProfileName(existing.getEventProfileName())
                .eventProfileUri(existing.getEventProfileUri())
                .status(WebhookStatus.INACTIVE)
                .eventsSubscribed(existing.getEventsSubscribed())
                .properties(existing.getProperties())
                .build();
        when(mockWebhookManagementService.getWebhook("stream-uuid-9", TENANT_DOMAIN)).thenReturn(existing);
        when(mockWebhookManagementService.deactivateWebhook("stream-uuid-9", TENANT_DOMAIN))
                .thenReturn(deactivated);

        StreamStatus statusUpdate = new StreamStatus.Builder()
                .streamId("stream-uuid-9")
                .status("disabled")
                .reason("pausing for maintenance")
                .build();

        StreamStatus result = service.updateStatus(statusUpdate, TENANT_DOMAIN);

        verify(mockWebhookManagementService).deactivateWebhook("stream-uuid-9", TENANT_DOMAIN);
        assertEquals(result.getStatus(), "disabled");
    }

    @Test(expectedExceptions = SSFStreamManagementClientException.class,
            expectedExceptionsMessageRegExp = "Unsupported stream status.")
    public void testUpdateStatusPausedRejected() throws Exception {

        StreamStatus statusUpdate = new StreamStatus.Builder()
                .streamId("stream-uuid-10")
                .status("paused")
                .build();

        try {
            service.updateStatus(statusUpdate, TENANT_DOMAIN);
        } finally {
            verify(mockWebhookManagementService, never()).activateWebhook(anyString(), anyString());
            verify(mockWebhookManagementService, never()).deactivateWebhook(anyString(), anyString());
            verify(mockWebhookManagementService, never()).getWebhook(anyString(), anyString());
        }
    }

    private Webhook buildWebhook(String uuid, List<String> aud, String description, List<String> channelUris) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("aud", aud);
        if (description != null) {
            properties.put("description", description);
        }

        List<Subscription> subscriptions = null;
        if (channelUris != null) {
            subscriptions = Collections.singletonList(Subscription.builder()
                    .channelUri(channelUris.get(0))
                    .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                    .build());
        }

        return new Webhook.Builder()
                .uuid(uuid)
                .endpoint("https://receiver.example.com/events")
                .name("SSF Stream - test")
                .secret("test-secret")
                .eventProfileName("CAEP")
                .eventProfileUri("https://schemas.openid.net/secevent/caep")
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(subscriptions)
                .properties(properties)
                .build();
    }
}
