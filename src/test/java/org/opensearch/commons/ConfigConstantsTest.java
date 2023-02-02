/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.MockSecureSettings;
import org.opensearch.common.settings.Settings;

public class ConfigConstantsTest {
    private void assertPasswords(Settings settings, String expectedPassword, String expectedKeyPassword) {
        final var password = ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD_SETTING.get(settings);
        Assertions.assertEquals(expectedPassword, password.toString());

        final var keyPassword = ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD_SETTING.get(settings);
        Assertions.assertEquals(expectedKeyPassword, keyPassword.toString());
    }

    @Test
    public void testLegacyHttpKeystorePassword() {
        final var settings = Settings
            .builder()
            .put("plugins.security.ssl.http.keystore_password", "legacy-password")
            .put("plugins.security.ssl.http.keystore_keypassword", "legacy-keypassword")
            .build();

        assertPasswords(settings, "legacy-password", "legacy-keypassword");
    }

    @Test
    public void testSecureHttpKeystorePassword() {
        final var mockSecureSettings = new MockSecureSettings();
        // deliberately not using constants here to verify correct concatenation of legacy + _secure suffix
        mockSecureSettings.setString("plugins.security.ssl.http.keystore_password_secure", "password");
        mockSecureSettings.setString("plugins.security.ssl.http.keystore_keypassword_secure", "keypassword");

        final var settings = Settings
            .builder()
            // check priority that secure variants are taken over the legacy ones
            .put("plugins.security.ssl.http.keystore_password", "legacy-password")
            .put("plugins.security.ssl.http.keystore_keypassword", "legacy-keypassword")
            .setSecureSettings(mockSecureSettings)
            .build();

        assertPasswords(settings, "password", "keypassword");
    }
}
