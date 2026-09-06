/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Webhook

internal class NotificationConfigReferenceTests {
    @Test
    fun `notification URL models accept complete keystore references`() {
        val reference = "\${keystore:webhook.url}"

        assertDoesNotThrow {
            Slack(reference)
            Chime(reference)
            MicrosoftTeams(reference)
            Webhook(reference)
        }
    }

    @Test
    fun `ordinary HTTP URLs remain valid`() {
        assertDoesNotThrow { validateUrlOrKeystoreReference("https://example.com/webhook") }
    }

    @Test
    fun `embedded keystore references are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateUrlOrKeystoreReference("https://example.com/\${keystore:webhook.path}")
        }
    }

    @Test
    fun `malformed keystore references are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateUrlOrKeystoreReference("\${keystore:webhook/path}")
        }
    }

    @Test
    fun `keystore reference matching requires the complete value`() {
        assertTrue(isKeystoreReference("\${keystore:webhook.url}"))
        assertFalse(isKeystoreReference("https://example.com/\${keystore:webhook.url}"))
    }
}
