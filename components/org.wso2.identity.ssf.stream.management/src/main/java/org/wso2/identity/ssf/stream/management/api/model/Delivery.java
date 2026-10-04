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

package org.wso2.identity.ssf.stream.management.api.model;

/**
 * Represents the delivery method configuration for an SSF stream, as defined by the
 * Shared Signals Framework Stream Configuration API.
 */
public class Delivery {

    private final String method;
    private final String endpointUrl;

    private Delivery(Builder builder) {

        this.method = builder.method;
        this.endpointUrl = builder.endpointUrl;
    }

    public String getMethod() {

        return method;
    }

    public String getEndpointUrl() {

        return endpointUrl;
    }

    /**
     * Builder for {@link Delivery}.
     */
    public static class Builder {

        private String method;
        private String endpointUrl;

        public Builder method(String method) {

            this.method = method;
            return this;
        }

        public Builder endpointUrl(String endpointUrl) {

            this.endpointUrl = endpointUrl;
            return this;
        }

        public Delivery build() {

            return new Delivery(this);
        }
    }
}
