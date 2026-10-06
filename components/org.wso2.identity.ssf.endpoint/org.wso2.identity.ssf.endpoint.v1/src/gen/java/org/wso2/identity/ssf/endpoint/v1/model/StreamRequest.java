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

package org.wso2.identity.ssf.endpoint.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.identity.ssf.endpoint.v1.model.Delivery;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class StreamRequest  {
  
    private String streamId;
    private Delivery delivery;
    private List<String> eventsRequested = null;

    private String description;

    /**
    * Stream ID. Ignored on create (a new one is always generated); required on update/replace to identify the stream being modified. 
    **/
    public StreamRequest streamId(String streamId) {

        this.streamId = streamId;
        return this;
    }
    
    @ApiModelProperty(example = "eeb8c1a2-3f4d-4e5b-8c6f-7d8e9f0a1b2c", value = "Stream ID. Ignored on create (a new one is always generated); required on update/replace to identify the stream being modified. ")
    @JsonProperty("stream_id")
    @Valid
    public String getStreamId() {
        return streamId;
    }
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /**
    **/
    public StreamRequest delivery(Delivery delivery) {

        this.delivery = delivery;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("delivery")
    @Valid
    public Delivery getDelivery() {
        return delivery;
    }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    /**
    * The event types the receiver is requesting delivery of.
    **/
    public StreamRequest eventsRequested(List<String> eventsRequested) {

        this.eventsRequested = eventsRequested;
        return this;
    }
    
    @ApiModelProperty(example = "[\"https://schemas.openid.net/secevent/caep/event-type/session-revoked\"]", value = "The event types the receiver is requesting delivery of.")
    @JsonProperty("events_requested")
    @Valid
    public List<String> getEventsRequested() {
        return eventsRequested;
    }
    public void setEventsRequested(List<String> eventsRequested) {
        this.eventsRequested = eventsRequested;
    }

    public StreamRequest addEventsRequestedItem(String eventsRequestedItem) {
        if (this.eventsRequested == null) {
            this.eventsRequested = new ArrayList<>();
        }
        this.eventsRequested.add(eventsRequestedItem);
        return this;
    }

        /**
    * A human-readable description of this stream.
    **/
    public StreamRequest description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "My stream", value = "A human-readable description of this stream.")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StreamRequest streamRequest = (StreamRequest) o;
        return Objects.equals(this.streamId, streamRequest.streamId) &&
            Objects.equals(this.delivery, streamRequest.delivery) &&
            Objects.equals(this.eventsRequested, streamRequest.eventsRequested) &&
            Objects.equals(this.description, streamRequest.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, delivery, eventsRequested, description);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class StreamRequest {\n");
        
        sb.append("    streamId: ").append(toIndentedString(streamId)).append("\n");
        sb.append("    delivery: ").append(toIndentedString(delivery)).append("\n");
        sb.append("    eventsRequested: ").append(toIndentedString(eventsRequested)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

