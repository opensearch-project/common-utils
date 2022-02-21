/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SendNotificationResponseTests {

    @Test
    fun `Create response serialize and deserialize transport object should be equal`() {
        val configResponse = SendNotificationResponse("sample_notification_id")
        val recreatedObject = recreateObject(configResponse) { SendNotificationResponse(it) }
        assertEquals(configResponse.notificationId, recreatedObject.notificationId)
    }

    @Test
    fun `Create response serialize and deserialize using json object should be equal`() {
        val configResponse = SendNotificationResponse("sample_notification_id")
        val jsonString = getJsonString(configResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(configResponse.notificationId, recreatedObject.notificationId)
    }

    @Test
    fun `Create response should deserialize json object using parser`() {
        val notificationId = "sample_notification_id"
        val jsonString = "{\"event_id\":\"$notificationId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(notificationId, recreatedObject.notificationId)
    }

    @Test
    fun `Create response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should throw exception when notificationId is replace with notificationId2 in json object`() {
        val jsonString = "{\"event_id2\":\"sample_notification_id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should safely ignore extra field in json object`() {
        val notificationId = "sample_notification_id"
        val jsonString = """
        {
            "event_id":"$notificationId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(notificationId, recreatedObject.notificationId)
    }
}
