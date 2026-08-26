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

    private SSFAdapterConstants() {

    }
}
