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

import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

/**
 * SSF Stream Management service component's value holder.
 */
public class SSFStreamManagementDataHolder {

    private static final SSFStreamManagementDataHolder instance = new SSFStreamManagementDataHolder();

    private WebhookManagementService webhookManagementService;
    private WebhookMetadataService webhookMetadataService;

    private SSFStreamManagementDataHolder() {

    }

    public static SSFStreamManagementDataHolder getInstance() {

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
     * Get the webhook metadata service.
     *
     * @return Webhook metadata service.
     */
    public WebhookMetadataService getWebhookMetadataService() {

        return webhookMetadataService;
    }

    /**
     * Set the webhook metadata service.
     *
     * @param webhookMetadataService Webhook metadata service.
     */
    public void setWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        this.webhookMetadataService = webhookMetadataService;
    }
}
