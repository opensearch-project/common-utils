/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.rest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.OpenSearchException;
import org.opensearch.client.RestClient;
import org.opensearch.common.settings.Settings;

public class SecureRestClientBuilderTest {

    @Test
    public void testHttpRestClient() throws Exception {
        Settings settings = Settings.builder().put("http.port", 9200).put("plugins.security.ssl.http.enabled", false).build();
        SecureRestClientBuilder builder = new SecureRestClientBuilder(settings, null);
        RestClient restClient = builder.build();
        Assertions.assertNotNull(restClient);
        restClient.close();
    }

    @Test
    public void testHttpsRestClient() throws Exception {
        Settings settings = Settings
            .builder()
            .put("http.port", 9200)
            .put("plugins.security.ssl.http.enabled", true)
            .put("plugins.security.ssl.http.pemcert_filepath", "sample.pem")
            .build();

        String absolutePath = new File(getClass().getClassLoader().getResource("sample.pem").getFile()).getAbsolutePath();
        String configFolder = absolutePath.replace("sample.pem", "");

        SecureRestClientBuilder builder = new SecureRestClientBuilder(settings, Paths.get(configFolder));
        RestClient restClient = builder.build();
        Assertions.assertNotNull(restClient);
        restClient.close();
    }

    @Test
    public void testMissingPem() throws Exception {
        Settings settings = Settings.builder().put("http.port", 9200).put("plugins.security.ssl.http.enabled", true).build();
        String absolutePath = new File(getClass().getClassLoader().getResource("sample.pem").getFile()).getAbsolutePath();
        String configFolder = absolutePath.replace("sample.pem", "");
        new SecureRestClientBuilder(settings, Paths.get(configFolder)).build();
    }

    @Test
    public void testMissingConfigPath() {
        assertThrows(OpenSearchException.class, () -> {
            Settings settings = Settings
                .builder()
                .put("http.port", 9200)
                .put("plugins.security.ssl.http.enabled", true)
                .put("plugins.security.ssl.http.pemcert_filepath", "sample.pem")
                .build();
            new SecureRestClientBuilder(settings, Paths.get("sample.pem")).build();
        });
    }
}
