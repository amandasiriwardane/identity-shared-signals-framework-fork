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

package org.wso2.identity.webhook.caep.event.handler.internal.constant;

/**
 * Constants class for CAEP-specific channel and event-type URIs.
 */
public class CAEPConstants {

    public static class Channel {

        public static final String SESSION_ESTABLISHED_CHANNEL =
                "https://schemas.openid.net/secevent/caep/session-established";
        public static final String SESSION_PRESENTED_CHANNEL =
                "https://schemas.openid.net/secevent/caep/session-presented";
        public static final String SESSION_REVOKED_CHANNEL =
                "https://schemas.openid.net/secevent/caep/session-revoked";
        public static final String CREDENTIAL_CHANGE_CHANNEL = "https://schemas.openid.net/secevent/caep/credential";
    }

    public static class Event {

        public static final String SESSION_REVOKED_EVENT =
                "https://schemas.openid.net/secevent/caep/event-type/session-revoked";
        public static final String SESSION_CREATED_EVENT =
                "https://schemas.openid.net/secevent/caep/event-type/session-established";
        public static final String SESSION_PRESENTED_EVENT =
                "https://schemas.openid.net/secevent/caep/event-type/session-presented";
        public static final String CREDENTIAL_CHANGE_EVENT =
                "https://schemas.openid.net/secevent/caep/event-type/credential-change";
    }
}
