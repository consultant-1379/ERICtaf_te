package com.ericsson.cifwk.taf.executor.http.client;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientFactory {

    private static final int MAX_CONNECTION_COUNT = 10;
    private static final int ALLURE_SERVICE_TIMEOUT_MS = 350000;
    private static final int VALIDATE_CONNECTION_AFTER_MS = 3000;

    public static CloseableHttpClient createInstance() {
        return HttpClientBuilder.create()
            .setConnectionManager(createConnectionManager())
            .setDefaultRequestConfig(requestConfig())
            .setDefaultSocketConfig(socketConfig())
            .build();
    }
    public static CloseableHttpClient createSecureInstance(String teUsername, String tePassword) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(teUsername,tePassword);
        provider.setCredentials(AuthScope.ANY, credentials);
        return HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .setConnectionManager(createConnectionManager())
                .setDefaultRequestConfig(requestConfig())
                .setDefaultSocketConfig(socketConfig())
                .build();
    }

    private static PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultSocketConfig(socketConfig());
        connectionManager.setMaxTotal(MAX_CONNECTION_COUNT);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTION_COUNT);
        connectionManager.setValidateAfterInactivity(VALIDATE_CONNECTION_AFTER_MS);

        return connectionManager;
    }

    private static RequestConfig requestConfig() {
        return RequestConfig.custom()
            .setConnectTimeout(ALLURE_SERVICE_TIMEOUT_MS)
            .setConnectionRequestTimeout(ALLURE_SERVICE_TIMEOUT_MS)
            .setSocketTimeout(ALLURE_SERVICE_TIMEOUT_MS)
            .build();
    }

    private static SocketConfig socketConfig() {
        return SocketConfig.custom()
            .setTcpNoDelay(true)
            .build();
    }
}
