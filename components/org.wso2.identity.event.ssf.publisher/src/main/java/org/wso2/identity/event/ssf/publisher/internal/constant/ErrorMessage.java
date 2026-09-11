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

package org.wso2.identity.event.ssf.publisher.internal.constant;

/**
 * Error messages for webhook management.
 */
public enum ErrorMessage {

    // client errors

    // server errors
    ERROR_PUBLISHING_EVENT_INVALID_PAYLOAD("SSFADAPTER-65001", "Invalid payload provided.",
            "Event payload cannot be processed."),
    ERROR_GETTING_ASYNC_CLIENT("SSFADAPTER-65002", "Error getting the async client to publish events.",
            "Error preparing async client to publish events, tenant: %s."),
    ERROR_CREATING_SSL_CONTEXT("SSFADAPTER-65003", "Error while preparing SSL context for HTTP client.",
            "Server error encountered while preparing SSL context for HTTP client."),
    ERROR_SIGNING_SECURITY_EVENT_TOKEN("SSFADAPTER-65004", "Failed to sign the security event token.",
            "Failed to generate a JWS signature for the security event token. " +
                    "Ensure that the tenant's signing key is configured correctly."),
    ERROR_PUBLISHING_EVENT("SSFADAPTER-65005", "Error while publishing event.",
            "Error while publishing event to the receiver's endpoint using the SSF adapter."),
    ERROR_ACTIVE_WEBHOOKS_RETRIEVAL("SSFADAPTER-65006", "Error while retrieving active webhooks.",
            "Error while retrieving active webhooks.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    public String getCode() {

        return code;
    }

    public String getMessage() {

        return message;
    }

    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return code + " : " + message;
    }
}

