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
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.identity.event.ssf.publisher.internal.config.SSFAdapterConfiguration;
import org.wso2.identity.event.ssf.publisher.internal.service.impl.SSFEventPublisherImpl;

import static org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants.SSF_ADAPTER_NAME;

/**
 * SSF Outbound Event Adapter service component.
 */
@Component(
        name = "org.wso2.identity.event.ssf.publisher.internal.component.SSFAdapterServiceComponent",
        immediate = true)
public class SSFAdapterServiceComponent {

    private static final Log LOG = LogFactory.getLog(SSFAdapterServiceComponent.class);
    private static final String ERROR_CODE_ADAPTER_NOT_FOUND = "WEBHOOKMETA-66011";

    @Activate
    protected void activate(ComponentContext context) {

        try {
            SSFAdapterDataHolder.getInstance().setAdapterConfiguration(new SSFAdapterConfiguration(
                    SSFAdapterDataHolder.getInstance().getEventAdapterMetadataService()
                            .getAdapterByName(SSF_ADAPTER_NAME).getProperties()));
            if (SSFAdapterDataHolder.getInstance().getAdapterConfiguration().isAdapterEnabled()) {
                SSFEventPublisherImpl eventPublisherService = new SSFEventPublisherImpl();
                context.getBundleContext().registerService(EventPublisher.class.getName(),
                        eventPublisherService, null);
                SSFAdapterDataHolder.getInstance().setClientManager(new ClientManager());
                LOG.debug("Successfully activated the SSF adapter service.");
            }
        } catch (Throwable e) {
            if (e instanceof WebhookMetadataException &&
                    ERROR_CODE_ADAPTER_NOT_FOUND.equals(((WebhookMetadataException) e).getErrorCode())) {
                LOG.warn("SSF adapter is not enabled. " +
                        "Please enable the SSF adapter in the configuration file to use the SSF event publisher.");
            } else {
                LOG.error("Error while activating the SSF adapter service: " + e.getMessage(), e);
            }
        }
    }


    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Successfully de-activated the SSF adapter service.");
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
        LOG.debug("EventAdapterMetadataService set in SSFAdapterDataHolder bundle.");
    }

    protected void unsetEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        SSFAdapterDataHolder.getInstance().setEventAdapterMetadataService(null);
        LOG.debug("EventAdapterMetadataService unset in SSFAdapterDataHolder bundle.");
    }
}
