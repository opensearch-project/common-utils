/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class EmailRecipientStatusTests {
    @Test
    fun `EmailRecipientStatus serialize and deserialize should be equal`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("404", "invalid recipient")
        )
        val recreatedObject = recreateObject(sampleEmailRecipientStatus) { EmailRecipientStatus(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }

    @Test
    fun `EmailRecipientStatus serialize and deserialize using json should be equal`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("404", "invalid recipient")
        )
        val jsonString = getJsonString(sampleEmailRecipientStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }

    @Test
    fun `EmailRecipientStatus should throw exception for invalid recipient`() {
        assertThrows<IllegalArgumentException>("Should throw an Exception for invalid recipient Slack") {
            EmailRecipientStatus("slack", DeliveryStatus("404", "invalid recipient"))
        }
    }

    @Test
    fun `EmailRecipientStatus should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        }
    }

    @Test
    fun `EmailRecipientStatus should safely ignore extra field in json object`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("200", "Success")
        )
        val jsonString = """
        {
            "recipient": "sample@email.com",
            "delivery_status": {
                "status_code": "200",
                "status_text": "Success"
            },
            "extra": "field"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }
}
