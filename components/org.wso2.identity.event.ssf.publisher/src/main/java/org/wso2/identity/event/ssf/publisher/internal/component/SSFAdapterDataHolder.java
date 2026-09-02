package org.wso2.identity.event.ssf.publisher.internal.component;

import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.identity.event.ssf.publisher.internal.config.SSFAdapterConfiguration;

/**
 * SSF Outbound Event Adapter service component's value holder.
 */
public class SSFAdapterDataHolder {

    private static final SSFAdapterDataHolder instance = new SSFAdapterDataHolder();

    private WebhookManagementService webhookManagementService;
    private EventAdapterMetadataService eventAdapterMetadataService;
    private SSFAdapterConfiguration adapterConfiguration;
    private ClientManager clientManager;

    private SSFAdapterDataHolder() {

    }

    public static SSFAdapterDataHolder getInstance() {

        return instance;
    }

    public ClientManager getClientManager() {

        return clientManager;
    }

    public void setClientManager(ClientManager clientManager) {

        this.clientManager = clientManager;
    }

    public SSFAdapterConfiguration getAdapterConfiguration() {

        return adapterConfiguration;
    }

    public void setAdapterConfiguration(SSFAdapterConfiguration adapterConfiguration) {

        this.adapterConfiguration = adapterConfiguration;
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
