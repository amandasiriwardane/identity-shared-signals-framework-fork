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

package org.wso2.identity.ssf.stream.management.api.exception;

/**
 * SSF stream management exception.
 */
public class SSFStreamManagementException extends Exception {

    private final String errorCode;
    private final String description;

    public SSFStreamManagementException(String errorCode, String message, String description) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public SSFStreamManagementException(String errorCode, String message, String description, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public String getErrorCode() {

        return this.errorCode;
    }

    public String getDescription() {

        return this.description;
    }
}
