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
 * Represents the status of an SSF event stream, as defined by the Shared Signals Framework
 * Stream Status API. {@code status} is one of the spec's lowercase values - "enabled" or
 * "disabled" ("paused" is a valid spec value but is not supported, since it only has meaning
 * for poll-based delivery; requests using it are rejected before reaching this model).
 */
public class StreamStatus {

    private final String streamId;
    private final String status;
    private final String reason;

    private StreamStatus(Builder builder) {

        this.streamId = builder.streamId;
        this.status = builder.status;
        this.reason = builder.reason;
    }

    public String getStreamId() {

        return streamId;
    }

    public String getStatus() {

        return status;
    }

    public String getReason() {

        return reason;
    }

    /**
     * Builder for {@link StreamStatus}.
     */
    public static class Builder {

        private String streamId;
        private String status;
        private String reason;

        public Builder streamId(String streamId) {

            this.streamId = streamId;
            return this;
        }

        public Builder status(String status) {

            this.status = status;
            return this;
        }

        public Builder reason(String reason) {

            this.reason = reason;
            return this;
        }

        public StreamStatus build() {

            return new StreamStatus(this);
        }
    }
}
