/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SendNotificationResponseTests {

    @Test
    fun `Create response serialize and deserialize transport object should be equal`() {

        val sampleEvent = getSampleEvent()

        val recreatedObject = recreateObject(sampleEvent) { SendNotificationResponse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Create response serialize and deserialize using json object should be equal`() {

        val sampleEvent = getSampleEvent()

        val jsonString = getJsonString(sampleEvent)
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Create response should deserialize json object using parser`() {
        val sampleEvent = getSampleEvent()
        val jsonString = "{\"event_id\":\"$sampleEvent\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
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
        val sampleEvent = getSampleEvent()
        val jsonString = """
        {
            "event_id":"$sampleEvent",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    private fun getSampleEvent(): NotificationEvent {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )

        return NotificationEvent(sampleEventSource, listOf(sampleStatus))
    }
}
