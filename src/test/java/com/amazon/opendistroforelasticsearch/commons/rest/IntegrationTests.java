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

import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_ENABLED;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_SSL_HTTP_PEMCERT_FILEPATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Enable this after integration with security plugin is done")
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
            .put(OPENDISTRO_SECURITY_SSL_HTTP_ENABLED, true)
            .put(OPENDISTRO_SECURITY_SSL_HTTP_PEMCERT_FILEPATH, "sample.pem")
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
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
            .put(OPENDISTRO_SECURITY_SSL_HTTP_ENABLED, true)
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
            .build();

        RestClient client = new SecureRestClientBuilder(settings, configPath).build();
        Response response = client.performRequest(createSampleRequest());
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseBody.contains("user_name\":\"CN=kirk"));
        client.close();
    }
}
