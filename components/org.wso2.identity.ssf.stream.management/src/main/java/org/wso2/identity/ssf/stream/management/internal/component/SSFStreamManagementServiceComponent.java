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

package org.wso2.identity.ssf.stream.management.internal.component;

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
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.identity.ssf.stream.management.api.service.SSFStreamManagementService;
import org.wso2.identity.ssf.stream.management.internal.service.impl.SSFStreamManagementServiceImpl;

/**
 * SSF Stream Management service component.
 */
@Component(
        name = "org.wso2.identity.ssf.stream.management.internal.component.SSFStreamManagementServiceComponent",
        immediate = true)
public class SSFStreamManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(SSFStreamManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(SSFStreamManagementService.class.getName(),
                    new SSFStreamManagementServiceImpl(), null);
            LOG.debug("Successfully activated the SSF stream management service.");
        } catch (Throwable e) {
            LOG.error("Error while activating the SSF stream management service: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Successfully de-activated the SSF stream management service.");
    }

    @Reference(
            name = "webhook.management.service.component",
            service = WebhookManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetWebhookManagementService"
    )
    protected void setWebhookManagementService(WebhookManagementService webhookManagementService) {

        SSFStreamManagementDataHolder.getInstance().setWebhookManagementService(webhookManagementService);
    }

    protected void unsetWebhookManagementService(WebhookManagementService webhookManagementService) {

        SSFStreamManagementDataHolder.getInstance().setWebhookManagementService(null);
    }

    @Reference(
            name = "webhook.metadata.service.component",
            service = WebhookMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetWebhookMetadataService"
    )
    protected void setWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        SSFStreamManagementDataHolder.getInstance().setWebhookMetadataService(webhookMetadataService);
    }

    protected void unsetWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        SSFStreamManagementDataHolder.getInstance().setWebhookMetadataService(null);
    }
}
