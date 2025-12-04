/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        val original = SendNotificationResponse(getSampleEvent())

        val roundTripped = recreateObject(original) { SendNotificationResponse(it) }

        assertEquals(getJsonString(original), getJsonString(roundTripped))
    }

    @Test
    fun `Create response serialize and deserialize using json object should be equal`() {
        val original = SendNotificationResponse(getSampleEvent())

        val json = getJsonString(original)
        val parsed = createObjectFromJsonString(json) { SendNotificationResponse.parse(it) }

        assertEquals(getJsonString(original), getJsonString(parsed))
    }

    @Test
    fun `Create response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"

        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should throw exception when required fields are missing`() {
        // No event_source / status_list – just a bogus field
        val jsonString = """{"event_id2":"sample_notification_id"}"""

        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should safely ignore extra field in json object`() {
        val original = SendNotificationResponse(getSampleEvent())
        val baseJson = getJsonString(original)

        // Take the valid JSON and append extra fields at the root.
        // baseJson is something like:
        // {"event_source":{...},"status_list":[...]}
        val jsonWithExtras =
            baseJson.removeSuffix("}") +
                """
                    ,
                    "event_id":"legacy-id",
                    "extra_field_1":["extra", "value"],
                    "extra_field_2":{"extra":"value"},
                    "extra_field_3":"extra value 3"
                }
                """.trimIndent()

        val parsed = createObjectFromJsonString(jsonWithExtras) { SendNotificationResponse.parse(it) }

        // Extra fields should be ignored – core payload stays the same
        assertEquals(getJsonString(original), getJsonString(parsed))
    }

    private fun getSampleEvent(): NotificationEvent {
        val sampleEventSource =
            EventSource(
                "title",
                "reference_id",
                severity = SeverityType.INFO,
            )
        val sampleStatus =
            EventStatus(
                configId = "config_id",
                configName = "name",
                configType = ConfigType.SLACK,
                deliveryStatus = DeliveryStatus("404", "invalid recipient"),
            )

        return NotificationEvent(sampleEventSource, listOf(sampleStatus))
    }
}
