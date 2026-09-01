package org.wso2.identity.event.ssf.publisher.internal.component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.ssl.SSLContexts;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.IdentityKeyStoreResolver;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverException;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterException;
import org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants;
import org.wso2.identity.event.ssf.publisher.internal.util.SSFAdapterUtil;

import java.io.IOException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_CREATING_SSL_CONTEXT;
import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_GETTING_ASYNC_CLIENT;
import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_PUBLISHING_EVENT_INVALID_PAYLOAD;
import static org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage.ERROR_SIGNING_SECURITY_EVENT_TOKEN;

/**
 * Class to retrieve the HTTP Clients and sign outgoing Security Event Tokens.
 */
public class ClientManager {

    private static final Log LOG = LogFactory.getLog(ClientManager.class);
    private final CloseableHttpAsyncClient httpAsyncClient;
    /**
     * Global executor used for asynchronous callbacks.
     */
    private final Executor asyncCallbackExecutor;

    public ClientManager() throws SSFAdapterException {

        try {
            int maxConnections =
                    SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getDefaultMaxConnections();
            int maxConnectionsPerRoute =
                    SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getDefaultMaxConnectionsPerRoute();

            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setConnectTimeout(SSFAdapterDataHolder.getInstance().getAdapterConfiguration()
                            .getHTTPConnectionTimeout())
                    .setSoTimeout(
                            SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getHttpReadTimeout())
                    .setIoThreadCount(SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getIoThreadCount())
                    .build();
            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
            PoolingNHttpClientConnectionManager asyncConnectionManager =
                    new PoolingNHttpClientConnectionManager(ioReactor);
            asyncConnectionManager.setMaxTotal(maxConnections);
            asyncConnectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
            LOG.debug("PoolingNHttpClientConnectionManager created with maxConnections: " + maxConnections +
                    " and maxConnectionsPerRoute: " + maxConnectionsPerRoute);
            RequestConfig config = createRequestConfig();

            HttpAsyncClientBuilder httpAsyncClientBuilder = HttpAsyncClients.custom()
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(asyncConnectionManager)
                    .setSSLContext(createSSLContext());
            httpAsyncClient = httpAsyncClientBuilder.build();
            httpAsyncClient.start();
            LOG.debug("HttpAsyncClient started with config: connectTimeout=" +
                    config.getConnectTimeout() + ", connectionRequestTimeout=" +
                    config.getConnectionRequestTimeout() + ", socketTimeout=" +
                    config.getSocketTimeout() + ", maxConnections=" +
                    asyncConnectionManager.getMaxTotal() + ", maxConnectionsPerRoute=" +
                    asyncConnectionManager.getDefaultMaxPerRoute());

            RejectedExecutionHandler handler = (r, executor) -> {
                LOG.error(
                        "Async callback queue is full; discarding task of publishing events. " +
                                "Please attend immediately.");
            };

            this.asyncCallbackExecutor = new ThreadPoolExecutor(
                    SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getExecutorCorePoolSize(),
                    SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getExecutorMaxPoolSize(),
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(SSFAdapterDataHolder.getInstance().getAdapterConfiguration()
                            .getExecutorQueueCapacity()),
                    Executors.defaultThreadFactory(),
                    handler);
        } catch (IOException e) {
            throw SSFAdapterUtil.handleServerException(ERROR_GETTING_ASYNC_CLIENT, e);
        }
    }

    /**
     * Get the executor for asynchronous callbacks.
     *
     * @return Executor instance for async callbacks.
     */
    public Executor getAsyncCallbackExecutor() {

        return asyncCallbackExecutor;
    }

    /**
     * Get the Max Retries for HTTP requests.
     *
     * @return Maximum number of retries.
     */
    public int getMaxRetries() {

        return SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getMaxRetries();
    }

    public CloseableHttpAsyncClient getHttpAsyncClient() {

        if (!httpAsyncClient.isRunning()) {
            LOG.debug("HttpAsyncClient is not running, starting client");
            httpAsyncClient.start();
        }
        return httpAsyncClient;
    }

    private RequestConfig createRequestConfig() {

        return RequestConfig.custom()
                .setConnectTimeout(SSFAdapterDataHolder.getInstance().getAdapterConfiguration()
                        .getHTTPConnectionTimeout())
                .setConnectionRequestTimeout(SSFAdapterDataHolder.getInstance().getAdapterConfiguration()
                        .getHttpConnectionRequestTimeout())
                .setSocketTimeout(
                        SSFAdapterDataHolder.getInstance().getAdapterConfiguration().getHttpReadTimeout())
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
    }

    private SSLContext createSSLContext() throws SSFAdapterException {

        try {
            return SSLContexts.custom().build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw SSFAdapterUtil.handleServerException(ERROR_CREATING_SSL_CONTEXT, e);
        }
    }

    /**
     * Create an HTTP POST request carrying a signed Security Event Token as its body.
     *
     * @param url        The receiver's endpoint URL.
     * @param claimsSet  The claims to sign and send.
     * @param tenantDomain The tenant whose signing key should be used.
     * @return A configured HttpPost instance.
     * @throws SSFAdapterException If an error occurs while signing or creating the request.
     */
    public HttpPost createHttpPost(String url, JWTClaimsSet claimsSet, String tenantDomain)
            throws SSFAdapterException {

        HttpPost request = new HttpPost(url);
        request.setHeader(CONTENT_TYPE, SSFAdapterConstants.Http.SET_CONTENT_TYPE);

        String signedEvent = signSecurityEventToken(claimsSet, tenantDomain);
        try {
            request.setEntity(new StringEntity(signedEvent));
        } catch (IOException e) {
            throw SSFAdapterUtil.handleClientException(ERROR_PUBLISHING_EVENT_INVALID_PAYLOAD);
        }

        return request;
    }

    // Signs the security event token claims with the tenant's RSA key.
    private String signSecurityEventToken(JWTClaimsSet claimsSet, String tenantDomain)
            throws SSFAdapterException {

        try {
            Key privateKey = IdentityKeyStoreResolver.getInstance().getPrivateKey(
                    tenantDomain, IdentityKeyStoreResolverConstants.InboundProtocol.OAUTH);
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(new JOSEObjectType(SSFAdapterConstants.Http.SECEVENT_JWT_TYPE))
                    .build();
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new RSASSASigner((RSAPrivateKey) privateKey));
            return signedJWT.serialize();
        } catch (IdentityKeyStoreResolverException | JOSEException e) {
            throw SSFAdapterUtil.handleServerException(ERROR_SIGNING_SECURITY_EVENT_TOKEN, e);
        }
    }

    /**
     * Execute an HTTP POST request asynchronously.
     *
     * @param httpPost The HTTP POST request to execute.
     * @return A CompletableFuture containing the HTTP response.
     */
    public CompletableFuture<HttpResponse> executeAsync(HttpPost httpPost) {

        CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        getHttpAsyncClient().execute(httpPost, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {

                future.complete(result);
            }

            @Override
            public void failed(Exception ex) {

                future.completeExceptionally(
                        new IdentityRuntimeException(
                                "SSF publisher async http client execution failed for URL: " + httpPost.getURI(),
                                ex
                        )
                );
            }

            @Override
            public void cancelled() {

                future.cancel(true);
            }
        });

        return future;
    }
}

