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
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class Delivery  {
  
    private String method;
    private String endpointUrl;

    /**
    * The delivery method URI.
    **/
    public Delivery method(String method) {

        this.method = method;
        return this;
    }
    
    @ApiModelProperty(example = "urn:ietf:rfc:8935", required = true, value = "The delivery method URI.")
    @JsonProperty("method")
    @Valid
    @NotNull(message = "Property method cannot be null.")

    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }

    /**
    * The endpoint events are delivered to.
    **/
    public Delivery endpointUrl(String endpointUrl) {

        this.endpointUrl = endpointUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://receiver.example.com/events", required = true, value = "The endpoint events are delivered to.")
    @JsonProperty("endpoint_url")
    @Valid
    @NotNull(message = "Property endpointUrl cannot be null.")

    public String getEndpointUrl() {
        return endpointUrl;
    }
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Delivery delivery = (Delivery) o;
        return Objects.equals(this.method, delivery.method) &&
            Objects.equals(this.endpointUrl, delivery.endpointUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, endpointUrl);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Delivery {\n");
        
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    endpointUrl: ").append(toIndentedString(endpointUrl)).append("\n");
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

