/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class NotificationEventTests {

    @Test
    fun `Notification event serialize and deserialize should be equal`() {
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
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val recreatedObject = recreateObject(sampleEvent) { NotificationEvent(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event serialize and deserialize using json should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val jsonString = getJsonString(sampleEvent)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event should safely ignore extra field in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO,
            tags = listOf("tag1", "tag2")
        )
        val status1 = EventStatus(
            "config_id1",
            "name 1",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val status2 = EventStatus(
            "config_id2",
            "name 2",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("503", "service unavailable")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(status1, status2))
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            },
            "status_list":[
                {
                   "config_id":"config_id1",
                   "config_type":"chime",
                   "config_name":"name 1",
                   "delivery_status":
                   {
                        "status_code":"200",
                        "status_text":"success"
                   }
                },
                {
                   "config_id":"config_id2",
                   "config_type":"slack",
                   "config_name":"name 2",
                   "delivery_status":
                   {
                        "status_code":"503",
                        "status_text":"service unavailable"
                   }
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event throw exception if event source is absent`() {
        val jsonString = """
        {
            "status_list":[
                {
                   "config_id":"config_id",
                   "config_type":"chime",
                   "config_name":"name",
                   "delivery_status":
                   {
                        "status_code":"200",
                        "status_text":"success"
                   }
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }

    @Test
    fun `Notification event throw exception if status_list is absent`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }

    @Test
    fun `Notification event throw exception if status_list is empty`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            },
            "status_list":[]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }
}
