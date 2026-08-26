package org.wso2.identity.event.ssf.publisher.internal.config;

import org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants;

import java.util.Map;

/**
 * SSF Adapter Configuration.
 */
public class SSFAdapterConfiguration {

    private static final String ADAPTER_ENABLED_CONFIG = "enabled";
    private static final String HTTP_CONNECTION_TIMEOUT = "httpConnectionTimeout";
    private static final String HTTP_READ_TIMEOUT = "httpReadTimeout";
    private static final String HTTP_CONNECTION_REQUEST_TIMEOUT = "httpConnectionRequestTimeout";
    private static final String DEFAULT_MAX_CONNECTIONS = "defaultMaxConnections";
    private static final String DEFAULT_MAX_CONNECTIONS_PER_ROUTE = "defaultMaxConnectionsPerRoute";
    private static final String MAX_RETRIES = "maxRetries";
    private static final String IO_THREAD_COUNT = "ioThreadCount";
    private static final String EXECUTOR_CORE_POOL_SIZE = "executorCorePoolSize";
    private static final String EXECUTOR_MAX_POOL_SIZE = "executorMaxPoolSize";
    private static final String EXECUTOR_QUEUE_CAPACITY = "executorQueueCapacity";

    private final boolean adapterEnabled;
    private final int httpConnectionTimeout;
    private final int httpReadTimeout;
    private final int httpConnectionRequestTimeout;
    private final int defaultMaxConnections;
    private final int defaultMaxConnectionsPerRoute;
    private final int maxRetries;
    private final int ioThreadCount;
    private final int executorCorePoolSize;
    private final int executorMaxPoolSize;
    private final int executorQueueCapacity;

    /**
     * Initialize the {@link SSFAdapterConfiguration}.
     *
     * @param properties Map of properties to initialize the configuration.
     */
    public SSFAdapterConfiguration(Map<String, String> properties) {

        this.adapterEnabled = Boolean.parseBoolean(properties.get(ADAPTER_ENABLED_CONFIG));

        this.httpConnectionTimeout = parseIntOrDefault(
                properties.get(HTTP_CONNECTION_TIMEOUT),
                SSFAdapterConstants.Http.DEFAULT_HTTP_CONNECTION_TIMEOUT);
        this.httpReadTimeout = parseIntOrDefault(
                properties.get(HTTP_READ_TIMEOUT),
                SSFAdapterConstants.Http.DEFAULT_HTTP_READ_TIMEOUT);
        this.httpConnectionRequestTimeout = parseIntOrDefault(
                properties.get(HTTP_CONNECTION_REQUEST_TIMEOUT),
                SSFAdapterConstants.Http.DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT);
        this.defaultMaxConnections = parseIntOrDefault(
                properties.get(DEFAULT_MAX_CONNECTIONS),
                SSFAdapterConstants.Http.DEFAULT_HTTP_MAX_CONNECTIONS);
        this.defaultMaxConnectionsPerRoute = parseIntOrDefault(
                properties.get(DEFAULT_MAX_CONNECTIONS_PER_ROUTE),
                SSFAdapterConstants.Http.DEFAULT_HTTP_MAX_CONNECTIONS_PER_ROUTE);
        this.maxRetries = parseIntOrDefault(
                properties.get(MAX_RETRIES),
                SSFAdapterConstants.Http.DEFAULT_HTTP_MAX_RETRIES);
        this.ioThreadCount = parseIntOrDefault(
                properties.get(IO_THREAD_COUNT),
                SSFAdapterConstants.Http.DEFAULT_HTTP_IO_THREAD_COUNT);
        this.executorCorePoolSize = parseIntOrDefault(
                properties.get(EXECUTOR_CORE_POOL_SIZE),
                SSFAdapterConstants.Http.DEFAULT_HTTP_EXECUTOR_CORE_POOL_SIZE);
        this.executorMaxPoolSize = parseIntOrDefault(
                properties.get(EXECUTOR_MAX_POOL_SIZE),
                SSFAdapterConstants.Http.DEFAULT_HTTP_EXECUTOR_MAX_POOL_SIZE);
        this.executorQueueCapacity = parseIntOrDefault(
                properties.get(EXECUTOR_QUEUE_CAPACITY),
                SSFAdapterConstants.Http.DEFAULT_HTTP_EXECUTOR_QUEUE_CAPACITY);
    }

    private int parseIntOrDefault(String value, int defaultValue) {

        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Getter method to return adapter enable configuration.
     *
     * @return whether adapter is enabled in the configurations.
     */
    public boolean isAdapterEnabled() {

        return adapterEnabled;
    }

    /**
     * Returns the HTTP connection timeout.
     *
     * @return HTTP connection timeout.
     */
    public int getHTTPConnectionTimeout() {

        return httpConnectionTimeout;
    }

    /**
     * Returns the HTTP read timeout.
     *
     * @return HTTP Read Timeout.
     */
    public int getHttpReadTimeout() {

        return httpReadTimeout;
    }

    /**
     * Returns the http connection request timeout.
     *
     * @return http connection request timeout.
     */    
    public int getHttpConnectionRequestTimeout() {

        return httpConnectionRequestTimeout;
    }

    /**
     * Returns the default max connections.
     *
     * @return default max connections.
     */
    public int getDefaultMaxConnections() {

        return defaultMaxConnections;
    }

    /**
     * Returns the default max connections per route.
     *
     * @return default max connections per route.
     */
    public int getDefaultMaxConnectionsPerRoute() {

        return defaultMaxConnectionsPerRoute;
    }

    /**
     * Returns max retries for HTTP requests.
     *
     * @return max retries.
     */
    public int getMaxRetries() {

        return maxRetries;
    }

    /**
     * Returns IO thread count for the async HTTP reactor.
     *
     * @return IO thread count.
     */
    public int getIoThreadCount() {

        return ioThreadCount;
    }

    /**
     * Returns the async callback executor core pool size.
     *
     * @return pool size.
     */
    public int getExecutorCorePoolSize() {

        return executorCorePoolSize;
    }

    /**
     * Returns the async callback executor max pool size.
     *
     * @return pool size.
     */
    public int getExecutorMaxPoolSize() {

        return executorMaxPoolSize;
    }

    /**
     * Returns the async callback executor queue capacity.
     *
     * @return queue capacity.
     */
    public int getExecutorQueueCapacity() {

        return executorQueueCapacity;
    }
}

