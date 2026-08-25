package org.wso2.identity.event.ssf.publisher.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

/**
 * SSF Outbound Event Adapter service component.
 */
@Component(
        name = "org.wso2.identity.event.ssf.publisher.internal.component.SSFAdapterServiceComponent",
        immediate = true)
public class SSFAdapterServiceComponent {

    private static final Log log = LogFactory.getLog(SSFAdapterServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("SSF adapter service component activated.");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Successfully de-activated the SSF adapter service.");
    }

    @Reference(
            name = "webhook.management.service.component",
            service = WebhookManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetWebhookManagementService"
    )
    protected void setWebhookManagementService(WebhookManagementService webhookManagementService) {

        SSFAdapterDataHolder.getInstance().setWebhookManagementService(webhookManagementService);
    }

    protected void unsetWebhookManagementService(WebhookManagementService webhookManagementService) {

        SSFAdapterDataHolder.getInstance().setWebhookManagementService(null);
    }

    @Reference(
            name = "identity.webhook.adapter.metadata.component",
            service = EventAdapterMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventAdapterMetadataService"
    )
    protected void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        SSFAdapterDataHolder.getInstance().setEventAdapterMetadataService(eventAdapterMetadataService);
        log.debug("EventAdapterMetadataService set in SSFAdapterDataHolder bundle.");
    }

    protected void unsetEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        SSFAdapterDataHolder.getInstance().setEventAdapterMetadataService(null);
        log.debug("EventAdapterMetadataService unset in SSFAdapterDataHolder bundle.");
    }
}
