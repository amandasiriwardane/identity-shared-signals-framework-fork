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

package org.wso2.identity.ssf.discovery.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * The SSF Transmitter Configuration Metadata document, served at
 * {@code /.well-known/ssf-configuration} per the Shared Signals Framework spec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"spec_version", "issuer", "jwks_uri", "delivery_methods_supported",
        "configuration_endpoint", "status_endpoint", "authorization_schemes"})
public class SSFProviderConfiguration {

    @JsonProperty("spec_version")
    private final String specVersion;

    private final String issuer;

    @JsonProperty("jwks_uri")
    private final String jwksUri;

    @JsonProperty("delivery_methods_supported")
    private final List<String> deliveryMethodsSupported;

    @JsonProperty("configuration_endpoint")
    private final String configurationEndpoint;

    @JsonProperty("status_endpoint")
    private final String statusEndpoint;

    @JsonProperty("authorization_schemes")
    private final List<AuthorizationScheme> authorizationSchemes;

    private SSFProviderConfiguration(Builder builder) {

        this.specVersion = builder.specVersion;
        this.issuer = builder.issuer;
        this.jwksUri = builder.jwksUri;
        this.deliveryMethodsSupported = builder.deliveryMethodsSupported;
        this.configurationEndpoint = builder.configurationEndpoint;
        this.statusEndpoint = builder.statusEndpoint;
        this.authorizationSchemes = builder.authorizationSchemes;
    }

    public String getSpecVersion() {

        return specVersion;
    }

    public String getIssuer() {

        return issuer;
    }

    public String getJwksUri() {

        return jwksUri;
    }

    public List<String> getDeliveryMethodsSupported() {

        return deliveryMethodsSupported;
    }

    public String getConfigurationEndpoint() {

        return configurationEndpoint;
    }

    public String getStatusEndpoint() {

        return statusEndpoint;
    }

    public List<AuthorizationScheme> getAuthorizationSchemes() {

        return authorizationSchemes;
    }

    /**
     * Builder for {@link SSFProviderConfiguration}.
     */
    public static class Builder {

        private String specVersion;
        private String issuer;
        private String jwksUri;
        private List<String> deliveryMethodsSupported;
        private String configurationEndpoint;
        private String statusEndpoint;
        private List<AuthorizationScheme> authorizationSchemes;

        public Builder specVersion(String specVersion) {

            this.specVersion = specVersion;
            return this;
        }

        public Builder issuer(String issuer) {

            this.issuer = issuer;
            return this;
        }

        public Builder jwksUri(String jwksUri) {

            this.jwksUri = jwksUri;
            return this;
        }

        public Builder deliveryMethodsSupported(List<String> deliveryMethodsSupported) {

            this.deliveryMethodsSupported = deliveryMethodsSupported;
            return this;
        }

        public Builder configurationEndpoint(String configurationEndpoint) {

            this.configurationEndpoint = configurationEndpoint;
            return this;
        }

        public Builder statusEndpoint(String statusEndpoint) {

            this.statusEndpoint = statusEndpoint;
            return this;
        }

        public Builder authorizationSchemes(List<AuthorizationScheme> authorizationSchemes) {

            this.authorizationSchemes = authorizationSchemes;
            return this;
        }

        public SSFProviderConfiguration build() {

            return new SSFProviderConfiguration(this);
        }
    }

    /**
     * A single supported authorization scheme, identified by the URN of the protocol
     * specification it implements (e.g. {@code urn:ietf:rfc:6749} for OAuth 2.0).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthorizationScheme {

        @JsonProperty("spec_urn")
        private final String specUrn;

        public AuthorizationScheme(String specUrn) {

            this.specUrn = specUrn;
        }

        public String getSpecUrn() {

            return specUrn;
        }
    }
}
