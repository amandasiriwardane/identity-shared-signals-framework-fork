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

package org.wso2.identity.ssf.discovery.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.identity.ssf.discovery.model.SSFProviderConfiguration;
import org.wso2.identity.ssf.discovery.model.SSFProviderConfiguration.AuthorizationScheme;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve the SSF Transmitter Configuration Metadata document at
 * {@code /.well-known/ssf-configuration}, per the Shared Signals Framework spec. Tenant
 * resolution (e.g. for {@code /t/{tenant}/.well-known/ssf-configuration}) is handled by the
 * server's generic tenant-qualified URL machinery before this servlet is reached, the same way
 * it is for {@code /.well-known/openid-configuration}.
 */
@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/.well-known/ssf-configuration",
                "osgi.http.whiteboard.servlet.name=SSFDiscovery",
                "osgi.http.whiteboard.servlet.asyncSupported=true"
        }
)
public class SSFDiscoveryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(SSFDiscoveryServlet.class);

    private static final String SPEC_VERSION = "1_0";
    private static final String OAUTH2_AUTHORIZATION_SCHEME_URN = "urn:ietf:rfc:6749";
    private static final String SUPPORTED_DELIVERY_METHOD = "urn:ietf:rfc:8935";
    private static final String JWKS_PATH = "/oauth2/jwks";
    private static final String CONFIGURATION_PATH = "/ssf/stream";
    private static final String STATUS_PATH = "/ssf/stream/status";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
        try {
            String issuer = ServiceURLBuilder.create().setTenant(tenantDomain).build()
                    .getAbsolutePublicUrlWithoutPath();

            SSFProviderConfiguration configuration = new SSFProviderConfiguration.Builder()
                    .specVersion(SPEC_VERSION)
                    .issuer(issuer)
                    .jwksUri(issuer + JWKS_PATH)
                    .deliveryMethodsSupported(Collections.singletonList(SUPPORTED_DELIVERY_METHOD))
                    .configurationEndpoint(issuer + CONFIGURATION_PATH)
                    .statusEndpoint(issuer + STATUS_PATH)
                    .authorizationSchemes(Collections.singletonList(
                            new AuthorizationScheme(OAUTH2_AUTHORIZATION_SCHEME_URN)))
                    .build();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(CONTENT_TYPE_JSON);
            response.getWriter().print(MAPPER.writeValueAsString(configuration));
        } catch (URLBuilderException e) {
            LOG.error("Error while building the SSF discovery document for tenant: " + tenantDomain, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
