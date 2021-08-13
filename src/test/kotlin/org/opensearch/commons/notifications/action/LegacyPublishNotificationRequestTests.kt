/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.commons.notifications.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.commons.destination.message.LegacyChimeMessage
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.utils.recreateObject

internal class LegacyPublishNotificationRequestTests {

    private fun assertRequestEquals(
        expected: LegacyPublishNotificationRequest,
        actual: LegacyPublishNotificationRequest
    ) {
        assertEquals(expected.baseMessage.channelName, actual.baseMessage.channelName)
        assertEquals(expected.baseMessage.channelType, actual.baseMessage.channelType)
        assertEquals(expected.baseMessage.messageContent, actual.baseMessage.messageContent)
        assertEquals(expected.baseMessage.url, actual.baseMessage.url)
        assertEquals(expected.feature, actual.feature)
        assertNull(actual.validate())
    }

    @Test
    fun `publish request serialize and deserialize transport object should be equal`() {
        val baseMessage = LegacyChimeMessage.Builder("chime_message").withMessage("Hello world").withUrl("https://amazon.com").build()
        val request = LegacyPublishNotificationRequest(baseMessage, FEATURE_INDEX_MANAGEMENT)
        val recreatedObject = recreateObject(request) { LegacyPublishNotificationRequest(it) }
        assertRequestEquals(request, recreatedObject)
    }
}
