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

import java.io.File;
import java.nio.file.Paths;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Assert;
import org.junit.Test;

public class SecureRestClientBuilderTest {

    @Test
    public void testHttpRestClient() throws Exception {
        Settings settings = Settings.builder().put("http.port", 9200).put("opendistro_security.ssl.http.enabled", false).build();
        SecureRestClientBuilder builder = new SecureRestClientBuilder(settings, null);
        RestClient restClient = builder.build();
        Assert.assertNotNull(restClient);
        restClient.close();
    }

    @Test
    public void testHttpsRestClient() throws Exception {
        Settings settings = Settings
            .builder()
            .put("http.port", 9200)
            .put("opendistro_security.ssl.http.enabled", true)
            .put("opendistro_security.ssl.http.pemcert_filepath", "sample.pem")
            .build();

        String absolutePath = new File(getClass().getClassLoader().getResource("sample.pem").getFile()).getAbsolutePath();
        String configFolder = absolutePath.replace("sample.pem", "");

        SecureRestClientBuilder builder = new SecureRestClientBuilder(settings, Paths.get(configFolder));
        RestClient restClient = builder.build();
        Assert.assertNotNull(restClient);
        restClient.close();
    }

    @Test
    public void testMissingPem() throws Exception {
        Settings settings = Settings.builder().put("http.port", 9200).put("opendistro_security.ssl.http.enabled", true).build();
        String absolutePath = new File(getClass().getClassLoader().getResource("sample.pem").getFile()).getAbsolutePath();
        String configFolder = absolutePath.replace("sample.pem", "");
        new SecureRestClientBuilder(settings, Paths.get(configFolder)).build();
    }

    @Test(expected = ElasticsearchException.class)
    public void testMissingConfigPath() throws Exception {
        Settings settings = Settings
            .builder()
            .put("http.port", 9200)
            .put("opendistro_security.ssl.http.enabled", true)
            .put("opendistro_security.ssl.http.pemcert_filepath", "sample.pem")
            .build();
        new SecureRestClientBuilder(settings, Paths.get("sample.pem")).build();
    }
}
