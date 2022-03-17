/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.commons.destination.message.LegacyChimeMessage
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
        assertNull(actual.validate())
    }

    @Test
    fun `publish request serialize and deserialize transport object should be equal`() {
        val baseMessage = LegacyChimeMessage.Builder("chime_message").withMessage("Hello world").withUrl("https://amazon.com").build()
        val request = LegacyPublishNotificationRequest(baseMessage)
        val recreatedObject = recreateObject(request) { LegacyPublishNotificationRequest(it) }
        assertRequestEquals(request, recreatedObject)
    }
}
