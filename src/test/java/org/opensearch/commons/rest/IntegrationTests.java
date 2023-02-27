/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_ENABLED;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_PEMCERT_FILEPATH;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.common.settings.Settings;

@Disabled("Enable this after integration with security plugin is done")
public class IntegrationTests {

    private Request createSampleRequest() {
        Request request = new Request("GET", "/_opendistro/_security/authinfo");
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        request.setOptions(builder);
        return request;
    }

    @Test
    public void testCreateRestClientWithUser() throws Exception {
        RestClient client = new SecureRestClientBuilder("localhost", 9200, true, "admin", "admin").build();
        Response response = client.performRequest(createSampleRequest());
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseBody.contains("user_name\":\"admin"));
        client.close();
    }

    @Test
    public void testCreateRestClientWithCerts() throws Exception {
        String dir = new File(getClass().getClassLoader().getResource("sample.pem").getPath()).getParentFile().getAbsolutePath();
        Path configPath = Paths.get(dir);
        Settings settings = Settings
            .builder()
            .put("http.port", 9200)
            .put(OPENSEARCH_SECURITY_SSL_HTTP_ENABLED, true)
            .put(OPENSEARCH_SECURITY_SSL_HTTP_PEMCERT_FILEPATH, "sample.pem")
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
            .build();

        RestClient client = new SecureRestClientBuilder(settings, configPath).build();
        Response response = client.performRequest(createSampleRequest());
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseBody.contains("user_name\":\"CN=kirk"));
        client.close();
    }

    @Test
    public void testCreateRestClientWithoutPem() throws Exception {
        String dir = new File(getClass().getClassLoader().getResource("sample.pem").getPath()).getParentFile().getAbsolutePath();
        Path configPath = Paths.get(dir);
        Settings settings = Settings
            .builder()
            .put("http.port", 9200)
            .put(OPENSEARCH_SECURITY_SSL_HTTP_ENABLED, true)
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
            .build();

        RestClient client = new SecureRestClientBuilder(settings, configPath).build();
        Response response = client.performRequest(createSampleRequest());
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseBody.contains("user_name\":\"CN=kirk"));
        client.close();
    }
}
