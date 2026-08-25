package org.wso2.identity.event.ssf.publisher.internal.component;

import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

/**
 * SSF Outbound Event Adapter service component's value holder.
 */
public class SSFAdapterDataHolder {

    private static final SSFAdapterDataHolder instance = new SSFAdapterDataHolder();

    private WebhookManagementService webhookManagementService;
    private EventAdapterMetadataService eventAdapterMetadataService;

    private SSFAdapterDataHolder() {

    }

    public static SSFAdapterDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the webhook management service.
     *
     * @return Webhook management service.
     */
    public WebhookManagementService getWebhookManagementService() {

        return webhookManagementService;
    }

    /**
     * Set the webhook management service.
     *
     * @param webhookManagementService Webhook management service.
     */
    public void setWebhookManagementService(WebhookManagementService webhookManagementService) {

        this.webhookManagementService = webhookManagementService;
    }

    /**
     * Get the event adapter metadata service.
     *
     * @return EventAdapterMetadataService instance.
     */
    public EventAdapterMetadataService getEventAdapterMetadataService() {

        return eventAdapterMetadataService;
    }

    /**
     * Set the event adapter metadata service.
     *
     * @param eventAdapterMetadataService EventAdapterMetadataService instance.
     */
    public void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        this.eventAdapterMetadataService = eventAdapterMetadataService;
    }
}
