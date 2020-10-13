/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.commons.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;

import com.amazon.opendistroforelasticsearch.commons.ConfigConstants;

/**
 * Provides builder to create low-level and high-level REST client to make calls to Elasticsearch.
 *
 * Sample usage:
 *      SecureRestClientBuilder builder = new SecureRestClientBuilder(settings).build()
 *      RestClient restClient = builder.build();
 *
 * Other usage:
 *  RestClient restClient = new SecureRestClientBuilder("localhost", 9200, false)
 *                     .setUserPassword("admin", "admin")
 *                     .setTrustCerts(trustStorePath)
 *                     .build();
 *
 *
 * If https is enabled, creates RestClientBuilder using self-signed certificates or passed pem
 * as trusted.
 *
 * If https is not enabled, creates a http based client.
 */
public class SecureRestClientBuilder {

    private final boolean httpSSLEnabled;
    private final String user;
    private final String passwd;
    private final ArrayList<HttpHost> hosts = new ArrayList<>();

    private final Path configPath;
    private final Settings settings;

    private int defaultConnectTimeOutMSecs = 5000;
    private int defaultSoTimeoutMSecs = 10000;
    private int defaultConnRequestTimeoutMSecs = 0;

    private static final Logger log = LogManager.getLogger(SecureRestClientBuilder.class);

    /**
     * ONLY for integration tests.
     * @param host
     * @param port
     * @param httpSSLEnabled
     * @param user
     * @param passWord
     */
    public SecureRestClientBuilder(
        final String host,
        final int port,
        final boolean httpSSLEnabled,
        final String user,
        final String passWord
    ) {
        if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passWord)) {
            throw new IllegalArgumentException("Invalid user or password");
        }

        this.httpSSLEnabled = httpSSLEnabled;
        this.user = user;
        this.passwd = passWord;
        this.settings = Settings.EMPTY;
        this.configPath = null;
        hosts.add(new HttpHost(host, port, httpSSLEnabled ? ConfigConstants.HTTPS : ConfigConstants.HTTP));
    }

    /**
     * ONLY for integration tests.
     * @param httpHosts
     * @param httpSSLEnabled
     * @param user
     * @param passWord
     */
    public SecureRestClientBuilder(HttpHost[] httpHosts, final boolean httpSSLEnabled, final String user, final String passWord) {

        if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passWord)) {
            throw new IllegalArgumentException("Invalid user or password");
        }

        this.httpSSLEnabled = httpSSLEnabled;
        this.user = user;
        this.passwd = passWord;
        this.settings = Settings.EMPTY;
        this.configPath = null;
        hosts.addAll(Arrays.asList(httpHosts));
    }

    public SecureRestClientBuilder(Settings settings, Path configPath) {

        this.httpSSLEnabled = settings.getAsBoolean(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_ENABLED, false);
        this.settings = settings;
        this.configPath = configPath;
        this.user = null;
        this.passwd = null;
        String host = ConfigConstants.HOST_DEFAULT;
        int port = settings.getAsInt(ConfigConstants.HTTP_PORT, ConfigConstants.HTTP_PORT_DEFAULT);
        hosts.add(new HttpHost(host, port, httpSSLEnabled ? ConfigConstants.HTTPS : ConfigConstants.HTTP));
    }

    /**
     * Creates a low-level Rest client.
     * @return
     * @throws IOException
     */
    public RestClient build() throws IOException {
        return createRestClientBuilder().build();
    }

    /**
     * Creates a high-level Rest client.
     * @return
     * @throws IOException
     */
    public RestHighLevelClient buildHighlevelClient() throws IOException {
        return new RestHighLevelClient(createRestClientBuilder());
    }

    public SecureRestClientBuilder setConnectTimeout(int timeout) {
        this.defaultConnectTimeOutMSecs = timeout;
        return this;
    }

    public SecureRestClientBuilder setSocketTimeout(int timeout) {
        this.defaultSoTimeoutMSecs = timeout;
        return this;
    }

    public SecureRestClientBuilder setConnectionRequestTimeout(int timeout) {
        this.defaultConnRequestTimeoutMSecs = timeout;
        return this;
    }

    private RestClientBuilder createRestClientBuilder() throws IOException {
        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));

        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder
                    .setConnectTimeout(defaultConnectTimeOutMSecs)
                    .setSocketTimeout(defaultSoTimeoutMSecs)
                    .setConnectionRequestTimeout(defaultConnRequestTimeoutMSecs);
            }
        });

        final SSLContext sslContext;
        try {
            sslContext = createSSLContext();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IOException(ex);
        }
        final CredentialsProvider credentialsProvider = createCredsProvider();
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                if (sslContext != null) {
                    httpClientBuilder.setSSLContext(sslContext);
                }
                if (credentialsProvider != null) {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder;
            }
        });
        return builder;
    }

    private SSLContext createSSLContext() throws IOException, GeneralSecurityException {
        SSLContextBuilder builder = new SSLContextBuilder();
        if (httpSSLEnabled) {
            // Handle trust store
            String pemFile = getTrustPem();
            if (Strings.isNullOrEmpty(pemFile)) {
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            } else {
                String pem = resolve(pemFile, configPath);
                KeyStore trustStore = new TrustStore(pem).create();
                builder.loadTrustMaterial(trustStore, null);
            }

            // Handle key store.
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                builder.loadKeyMaterial(keyStore, getKeystorePasswd().toCharArray());
            }

        }
        return builder.build();
    }

    private CredentialsProvider createCredsProvider() {
        if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passwd))
            return null;

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, passwd));
        return credentialsProvider;
    }

    private String resolve(final String originalFile, final Path configPath) {
        String path = null;
        if (originalFile != null && originalFile.length() > 0) {
            path = configPath.resolve(originalFile).toAbsolutePath().toString();
            log.debug("Resolved {} to {} against {}", originalFile, path, configPath.toAbsolutePath().toString());
        }

        if (path == null || path.length() == 0) {
            throw new ElasticsearchException("Empty file path for " + originalFile);
        }

        if (Files.isDirectory(Paths.get(path), LinkOption.NOFOLLOW_LINKS)) {
            throw new ElasticsearchException("Is a directory: " + path + " Expected a file for " + originalFile);
        }

        if (!Files.isReadable(Paths.get(path))) {
            throw new ElasticsearchException(
                "Unable to read "
                    + path
                    + " ("
                    + Paths.get(path)
                    + "). Please make sure this files exists and is readable regarding to permissions. Property: "
                    + originalFile
            );
        }
        if ("".equals(path)) {
            path = null;
        }
        return path;
    }

    private String getTrustPem() {
        return settings.get(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_PEMCERT_FILEPATH, null);
    }

    private String getKeystorePasswd() {
        return settings.get(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, null);
    }

    private KeyStore getKeyStore() throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("jks");
        String keyStoreFile = settings.get(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, null);
        String passwd = settings.get(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, null);
        if (Strings.isNullOrEmpty(keyStoreFile) || Strings.isNullOrEmpty(passwd)) {
            return null;
        }
        String keyStorePath = resolve(keyStoreFile, configPath);
        try (InputStream is = Files.newInputStream(Paths.get(keyStorePath))) {
            keyStore.load(is, passwd.toCharArray());
        }
        return keyStore;
    }
}
