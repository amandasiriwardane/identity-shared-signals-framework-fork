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

public class StreamResponse  {
  
    private String streamId;
    private String iss;
    private List<String> aud = null;

    private Delivery delivery;
    private List<String> eventsSupported = null;

    private List<String> eventsRequested = null;

    private List<String> eventsDelivered = null;

    private String description;

    /**
    * The unique identifier of this stream.
    **/
    public StreamResponse streamId(String streamId) {

        this.streamId = streamId;
        return this;
    }
    
    @ApiModelProperty(example = "eeb8c1a2-3f4d-4e5b-8c6f-7d8e9f0a1b2c", value = "The unique identifier of this stream.")
    @JsonProperty("stream_id")
    @Valid
    public String getStreamId() {
        return streamId;
    }
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /**
    * The issuer of the SET tokens delivered on this stream.
    **/
    public StreamResponse iss(String iss) {

        this.iss = iss;
        return this;
    }
    
    @ApiModelProperty(example = "https://localhost:9443", value = "The issuer of the SET tokens delivered on this stream.")
    @JsonProperty("iss")
    @Valid
    public String getIss() {
        return iss;
    }
    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
    * The intended audience(s) of this stream.
    **/
    public StreamResponse aud(List<String> aud) {

        this.aud = aud;
        return this;
    }
    
    @ApiModelProperty(value = "The intended audience(s) of this stream.")
    @JsonProperty("aud")
    @Valid
    public List<String> getAud() {
        return aud;
    }
    public void setAud(List<String> aud) {
        this.aud = aud;
    }

    public StreamResponse addAudItem(String audItem) {
        if (this.aud == null) {
            this.aud = new ArrayList<>();
        }
        this.aud.add(audItem);
        return this;
    }

        /**
    **/
    public StreamResponse delivery(Delivery delivery) {

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
    * The event types the transmitter is capable of delivering.
    **/
    public StreamResponse eventsSupported(List<String> eventsSupported) {

        this.eventsSupported = eventsSupported;
        return this;
    }
    
    @ApiModelProperty(value = "The event types the transmitter is capable of delivering.")
    @JsonProperty("events_supported")
    @Valid
    public List<String> getEventsSupported() {
        return eventsSupported;
    }
    public void setEventsSupported(List<String> eventsSupported) {
        this.eventsSupported = eventsSupported;
    }

    public StreamResponse addEventsSupportedItem(String eventsSupportedItem) {
        if (this.eventsSupported == null) {
            this.eventsSupported = new ArrayList<>();
        }
        this.eventsSupported.add(eventsSupportedItem);
        return this;
    }

        /**
    * The event types the receiver is requesting delivery of.
    **/
    public StreamResponse eventsRequested(List<String> eventsRequested) {

        this.eventsRequested = eventsRequested;
        return this;
    }
    
    @ApiModelProperty(value = "The event types the receiver is requesting delivery of.")
    @JsonProperty("events_requested")
    @Valid
    public List<String> getEventsRequested() {
        return eventsRequested;
    }
    public void setEventsRequested(List<String> eventsRequested) {
        this.eventsRequested = eventsRequested;
    }

    public StreamResponse addEventsRequestedItem(String eventsRequestedItem) {
        if (this.eventsRequested == null) {
            this.eventsRequested = new ArrayList<>();
        }
        this.eventsRequested.add(eventsRequestedItem);
        return this;
    }

        /**
    * The event types the transmitter has agreed to deliver on this stream.
    **/
    public StreamResponse eventsDelivered(List<String> eventsDelivered) {

        this.eventsDelivered = eventsDelivered;
        return this;
    }
    
    @ApiModelProperty(value = "The event types the transmitter has agreed to deliver on this stream.")
    @JsonProperty("events_delivered")
    @Valid
    public List<String> getEventsDelivered() {
        return eventsDelivered;
    }
    public void setEventsDelivered(List<String> eventsDelivered) {
        this.eventsDelivered = eventsDelivered;
    }

    public StreamResponse addEventsDeliveredItem(String eventsDeliveredItem) {
        if (this.eventsDelivered == null) {
            this.eventsDelivered = new ArrayList<>();
        }
        this.eventsDelivered.add(eventsDeliveredItem);
        return this;
    }

        /**
    * A human-readable description of this stream.
    **/
    public StreamResponse description(String description) {

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
        StreamResponse streamResponse = (StreamResponse) o;
        return Objects.equals(this.streamId, streamResponse.streamId) &&
            Objects.equals(this.iss, streamResponse.iss) &&
            Objects.equals(this.aud, streamResponse.aud) &&
            Objects.equals(this.delivery, streamResponse.delivery) &&
            Objects.equals(this.eventsSupported, streamResponse.eventsSupported) &&
            Objects.equals(this.eventsRequested, streamResponse.eventsRequested) &&
            Objects.equals(this.eventsDelivered, streamResponse.eventsDelivered) &&
            Objects.equals(this.description, streamResponse.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, iss, aud, delivery, eventsSupported, eventsRequested, eventsDelivered, description);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class StreamResponse {\n");
        
        sb.append("    streamId: ").append(toIndentedString(streamId)).append("\n");
        sb.append("    iss: ").append(toIndentedString(iss)).append("\n");
        sb.append("    aud: ").append(toIndentedString(aud)).append("\n");
        sb.append("    delivery: ").append(toIndentedString(delivery)).append("\n");
        sb.append("    eventsSupported: ").append(toIndentedString(eventsSupported)).append("\n");
        sb.append("    eventsRequested: ").append(toIndentedString(eventsRequested)).append("\n");
        sb.append("    eventsDelivered: ").append(toIndentedString(eventsDelivered)).append("\n");
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

