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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
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
    private final int port;
    private final String host;

    private String trustCerts = null;
    private String user = null;
    private String passwd = null;

    public SecureRestClientBuilder(final String host, final int port, final boolean httpSSLEnabled) {
        this.host = host;
        this.port = port;
        this.httpSSLEnabled = httpSSLEnabled;
    }

    public SecureRestClientBuilder(Settings settings) {
        this(
            ConfigConstants.HOST_DEFAULT,
            settings.getAsInt(ConfigConstants.HTTP_PORT, ConfigConstants.HTTP_PORT_DEFAULT),
            settings.getAsBoolean(ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_ENABLED, false)
        );
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

    /**
     * Pass pem cert. If null passed, uses TrustSelfSignedStrategy (trust strategy that accepts self-signed certificates as trusted).
     * @param cert
     * @return
     */
    public SecureRestClientBuilder setTrustCerts(final String cert) {
        this.trustCerts = cert;
        return this;
    }

    /**
     * User name and password for credentials. ONLY for integ tests.
     * @param user
     * @param passwd
     * @return
     * @throws IOException
     */
    public SecureRestClientBuilder setUserPassword(final String user, final String passwd) throws IOException {
        if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passwd)) {
            throw new IOException("Invalid user or password");
        }
        this.user = user;
        this.passwd = passwd;
        return this;
    }

    private RestClientBuilder createRestClientBuilder() throws IOException {
        RestClientBuilder builder = RestClient.builder(createHttpHost());
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

    private HttpHost createHttpHost() {
        return new HttpHost(host, port, httpSSLEnabled ? ConfigConstants.HTTPS : ConfigConstants.HTTP);
    }

    private SSLContext createSSLContext() throws IOException, GeneralSecurityException {
        SSLContextBuilder builder = new SSLContextBuilder();
        if (httpSSLEnabled) {
            if (trustCerts == null) {
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            } else {
                final String effectiveKeyAlias = "al";
                X509Certificate[] trustCertificates;
                KeyStore trustStore;
                trustCertificates = PemReader.loadCertificatesFromFile(trustCerts);
                trustStore = PemReader.toTruststore(effectiveKeyAlias, trustCertificates);
                builder.loadTrustMaterial(trustStore, null);
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
}
