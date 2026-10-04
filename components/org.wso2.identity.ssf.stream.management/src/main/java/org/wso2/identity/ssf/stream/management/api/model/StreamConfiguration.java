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

import java.util.Collections;
import java.util.List;

/**
 * Represents an SSF event stream configuration, as defined by the Shared Signals Framework
 * Stream Configuration API. This is the external, spec-shaped view of a stream - internally,
 * a stream is persisted as a {@code org.wso2.carbon.identity.webhook.management.api.model.Webhook}.
 */
public class StreamConfiguration {

    private final String streamId;
    private final String iss;
    private final List<String> aud;
    private final Delivery delivery;
    private final List<String> eventsSupported;
    private final List<String> eventsRequested;
    private final List<String> eventsDelivered;
    private final String description;

    private StreamConfiguration(Builder builder) {

        this.streamId = builder.streamId;
        this.iss = builder.iss;
        this.aud = builder.aud;
        this.delivery = builder.delivery;
        this.eventsSupported = builder.eventsSupported;
        this.eventsRequested = builder.eventsRequested;
        this.eventsDelivered = builder.eventsDelivered;
        this.description = builder.description;
    }

    public String getStreamId() {

        return streamId;
    }

    public String getIss() {

        return iss;
    }

    public List<String> getAud() {

        return aud;
    }

    public Delivery getDelivery() {

        return delivery;
    }

    public List<String> getEventsSupported() {

        return eventsSupported;
    }

    public List<String> getEventsRequested() {

        return eventsRequested;
    }

    public List<String> getEventsDelivered() {

        return eventsDelivered;
    }

    public String getDescription() {

        return description;
    }

    /**
     * Builder for {@link StreamConfiguration}.
     */
    public static class Builder {

        private String streamId;
        private String iss;
        private List<String> aud = Collections.emptyList();
        private Delivery delivery;
        private List<String> eventsSupported = Collections.emptyList();
        private List<String> eventsRequested = Collections.emptyList();
        private List<String> eventsDelivered = Collections.emptyList();
        private String description;

        public Builder streamId(String streamId) {

            this.streamId = streamId;
            return this;
        }

        public Builder iss(String iss) {

            this.iss = iss;
            return this;
        }

        public Builder aud(List<String> aud) {

            this.aud = aud != null ? aud : Collections.emptyList();
            return this;
        }

        public Builder delivery(Delivery delivery) {

            this.delivery = delivery;
            return this;
        }

        public Builder eventsSupported(List<String> eventsSupported) {

            this.eventsSupported = eventsSupported != null ? eventsSupported : Collections.emptyList();
            return this;
        }

        public Builder eventsRequested(List<String> eventsRequested) {

            this.eventsRequested = eventsRequested != null ? eventsRequested : Collections.emptyList();
            return this;
        }

        public Builder eventsDelivered(List<String> eventsDelivered) {

            this.eventsDelivered = eventsDelivered != null ? eventsDelivered : Collections.emptyList();
            return this;
        }

        public Builder description(String description) {

            this.description = description;
            return this;
        }

        public StreamConfiguration build() {

            return new StreamConfiguration(this);
        }
    }
}
