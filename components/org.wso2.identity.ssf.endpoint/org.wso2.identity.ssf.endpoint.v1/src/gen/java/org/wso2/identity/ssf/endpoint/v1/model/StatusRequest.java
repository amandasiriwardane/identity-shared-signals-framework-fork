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
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class StatusRequest  {
  
    private String streamId;

@XmlType(name="StatusEnum")
@XmlEnum(String.class)
public enum StatusEnum {

    @XmlEnumValue("enabled") ENABLED(String.valueOf("enabled")), @XmlEnumValue("disabled") DISABLED(String.valueOf("disabled"));


    private String value;

    StatusEnum(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private StatusEnum status;
    private String reason;

    /**
    * Stream ID.
    **/
    public StatusRequest streamId(String streamId) {

        this.streamId = streamId;
        return this;
    }
    
    @ApiModelProperty(example = "eeb8c1a2-3f4d-4e5b-8c6f-7d8e9f0a1b2c", required = true, value = "Stream ID.")
    @JsonProperty("stream_id")
    @Valid
    @NotNull(message = "Property streamId cannot be null.")

    public String getStreamId() {
        return streamId;
    }
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /**
    * The stream&#39;s requested status.
    **/
    public StatusRequest status(StatusEnum status) {

        this.status = status;
        return this;
    }
    
    @ApiModelProperty(example = "disabled", required = true, value = "The stream's requested status.")
    @JsonProperty("status")
    @Valid
    @NotNull(message = "Property status cannot be null.")

    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    /**
    * A human-readable reason for the status change.
    **/
    public StatusRequest reason(String reason) {

        this.reason = reason;
        return this;
    }
    
    @ApiModelProperty(example = "pausing for maintenance", value = "A human-readable reason for the status change.")
    @JsonProperty("reason")
    @Valid
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatusRequest statusRequest = (StatusRequest) o;
        return Objects.equals(this.streamId, statusRequest.streamId) &&
            Objects.equals(this.status, statusRequest.status) &&
            Objects.equals(this.reason, statusRequest.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, status, reason);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class StatusRequest {\n");
        
        sb.append("    streamId: ").append(toIndentedString(streamId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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

