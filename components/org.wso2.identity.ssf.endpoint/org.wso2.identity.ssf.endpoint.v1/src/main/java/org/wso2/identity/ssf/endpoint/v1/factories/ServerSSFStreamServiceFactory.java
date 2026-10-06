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

package org.wso2.identity.ssf.endpoint.v1.factories;

import org.wso2.identity.ssf.endpoint.common.SSFStreamManagementServiceHolder;
import org.wso2.identity.ssf.endpoint.v1.core.ServerSSFStreamService;
import org.wso2.identity.ssf.stream.management.api.service.SSFStreamManagementService;

/**
 * Factory class for ServerSSFStreamService.
 */
public class ServerSSFStreamServiceFactory {

    private static final ServerSSFStreamService SERVICE;

    static {
        SSFStreamManagementService ssfStreamManagementService =
                SSFStreamManagementServiceHolder.getSSFStreamManagementService();
        if (ssfStreamManagementService == null) {
            throw new IllegalStateException("SSFStreamManagementService is not available from OSGi context.");
        }
        SERVICE = new ServerSSFStreamService(ssfStreamManagementService);
    }

    /**
     * Get ServerSSFStreamService service.
     *
     * @return ServerSSFStreamService service.
     */
    public static ServerSSFStreamService getServerSSFStreamService() {

        return SERVICE;
    }
}
