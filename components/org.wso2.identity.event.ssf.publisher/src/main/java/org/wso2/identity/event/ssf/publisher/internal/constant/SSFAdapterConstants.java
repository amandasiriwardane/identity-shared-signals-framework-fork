package org.wso2.identity.event.ssf.publisher.internal.constant;

/**
 * Keep constants required by the SSF Event Adapter.
 */
public class SSFAdapterConstants {

    public static final String SSF_ADAPTER_NAME = "ssfpublisher";

    /**
     * SSF Adapter related constants.
     */
    public static class Http {

        public static final String SET_CONTENT_TYPE = "application/secevent+jwt";
        public static final String SECEVENT_JWT_TYPE = "secevent+jwt";
        public static final Integer DEFAULT_HTTP_CONNECTION_TIMEOUT = 300;
        public static final Integer DEFAULT_HTTP_READ_TIMEOUT = 300;
        public static final Integer DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT = 300;
        public static final Integer DEFAULT_HTTP_MAX_CONNECTIONS = 20;
        public static final Integer DEFAULT_HTTP_MAX_CONNECTIONS_PER_ROUTE = 2;
        public static final Integer DEFAULT_HTTP_MAX_RETRIES = 2;
        public static final Integer DEFAULT_HTTP_IO_THREAD_COUNT = 5;
        public static final Integer DEFAULT_HTTP_EXECUTOR_CORE_POOL_SIZE = 5;
        public static final Integer DEFAULT_HTTP_EXECUTOR_MAX_POOL_SIZE = 15;
        public static final Integer DEFAULT_HTTP_EXECUTOR_QUEUE_CAPACITY = 150;
        
        private Http() {

        }
    }

    /**
     * Constants related to logging.
     */
    public static class LogConstants {

        private LogConstants() {

        }

        public static final String SSF_ADAPTER = "ssf-adapter";

        /**
         * Class related to Action IDs.
         */
        public static class ActionIDs {

            private ActionIDs() {

            }

            public static final String PUBLISH_EVENT = "publish-event";
        }

        /**
         * Class related to Input Keys.
         */
        public static class InputKeys {

            private InputKeys() {

            }

            public static final String ENDPOINT = "endpoint";
            public static final String EVENTS = "events";
            public static final String EVENT_URI = "eventUri";
            public static final String EVENT_PROFILE_NAME = "eventProfileName";
        }
    }

    private SSFAdapterConstants() {

    }
}
