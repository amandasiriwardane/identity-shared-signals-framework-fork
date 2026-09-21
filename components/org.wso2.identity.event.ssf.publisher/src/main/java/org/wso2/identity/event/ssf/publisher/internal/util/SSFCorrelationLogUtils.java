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

package org.wso2.identity.event.ssf.publisher.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the utility methods for adding correlation logs for the SSF publisher.
 */
public class SSFCorrelationLogUtils {

    private static final Log correlationLog = LogFactory.getLog("correlation");
    private static final String CORRELATION_LOG_SYSTEM_PROPERTY = "enableCorrelationLogs";
    private static final String CORRELATION_LOG_REQUEST_END = "HTTP-Out-Response";
    private static final String CORRELATION_LOG_SEPARATOR = "|";
    private static Boolean isEnableCorrelationLogs;

    public static void handleResponseCorrelationLog(HttpPost request, long requestStartTime, String... otherParams) {

        SSFCorrelationLogUtils.triggerCorrelationLogForResponse(request, requestStartTime, otherParams);
    }

    public static void triggerCorrelationLogForResponse(HttpEntityEnclosingRequestBase request, long requestStartTime,
                                                         String... otherParams) {

        if (isCorrelationLogsEnabled() && correlationLog.isInfoEnabled()) {
            long currentTime = System.currentTimeMillis();
            long timeTaken = currentTime - requestStartTime;

            List<String> logPropertiesList = new ArrayList<>();
            logPropertiesList.add(Long.toString(timeTaken));
            logPropertiesList.add(CORRELATION_LOG_REQUEST_END);
            logPropertiesList.add(Long.toString(requestStartTime));
            logPropertiesList.add(request.getMethod());
            logPropertiesList.add(request.getURI().getQuery());
            logPropertiesList.add(request.getURI().getPath());
            Collections.addAll(logPropertiesList, otherParams);

            correlationLog.info(createFormattedLog(logPropertiesList));
        }
    }

    private static boolean isCorrelationLogsEnabled() {

        if (isEnableCorrelationLogs == null) {
            isEnableCorrelationLogs = Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY));
        }
        return isEnableCorrelationLogs;
    }

    private static String createFormattedLog(List<String> logPropertiesList) {

        return String.join(CORRELATION_LOG_SEPARATOR, logPropertiesList);
    }

    /**
     * HTTP outgoing request status.
     */
    public enum RequestStatus {

        COMPLETED("completed"),
        FAILED("failed"),
        CANCELLED("cancelled");

        private final String status;

        RequestStatus(String status) {

            this.status = status;
        }

        public String getStatus() {

            return status;
        }
    }
}
