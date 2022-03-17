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
import java.time.Instant

internal class NotificationEventInfoTests {

    @Test
    fun `Event info serialize and deserialize with event object should be equal`() {
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
        val eventInfo = NotificationEventInfo(
            "event_id",
            Instant.now(),
            Instant.now(),
            sampleEvent
        )
        val recreatedObject = recreateObject(eventInfo) { NotificationEventInfo(it) }
        assertEquals(eventInfo, recreatedObject)
    }

    @Test
    fun `Event info serialize and deserialize using json event object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val eventInfo = NotificationEventInfo(
            "event_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleEvent
        )
        val jsonString = getJsonString(eventInfo)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        assertEquals(eventInfo, recreatedObject)
    }

    @Test
    fun `Event info should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val eventInfo = NotificationEventInfo(
            "event_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleEvent
        )
        val jsonString = """
        {
            "event_id":"event_id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":[]
                },
                "status_list":[
                    {
                       "config_id":"config_id",
                       "config_type":"slack",
                       "config_name":"name",
                       "delivery_status":
                       {
                            "status_code":"200",
                            "status_text":"success"
                       }
                    }
                ]
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        assertEquals(eventInfo, recreatedObject)
    }

    @Test
    fun `Event info should throw exception if event_id is empty`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "event_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationEventInfo(
                "",
                lastUpdatedTimeMs,
                createdTimeMs,
                sampleEvent
            )
        }
    }

    @Test
    fun `Event info should throw exception if event_id is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":["tag1", "tag2"]
                },
                "status_list":[
                    {
                       "event_id":"event_id",
                       "config_type":"slack",
                       "config_name":"name",
                       "delivery_status":
                       {
                            "status_code":"200",
                            "status_text":"success"
                       }
                    }
                ]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        }
    }

    @Test
    fun `Event info should throw exception if lastUpdatedTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "event_id":"event_id",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":["tag1", "tag2"]
                },
                "status_list":[
                    {
                       "event_id":"event_id",
                       "config_type":"slack",
                       "config_name":"name",
                       "delivery_status":
                       {
                            "status_code":"200",
                            "status_text":"success"
                       }
                    }
                ]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        }
    }

    @Test
    fun `Event info should throw exception if createdTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val jsonString = """
        {
            "event_id":"event_id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":["tag1", "tag2"]
                },
                "status_list":[
                    {
                       "event_id":"event_id",
                       "config_type":"slack",
                       "config_name":"name",
                       "delivery_status":
                       {
                            "status_code":"200",
                            "status_text":"success"
                       }
                    }
                ]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        }
    }

    @Test
    fun `Event info should throw exception if event is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "event_id":"event_id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventInfo.parse(it) }
        }
    }
}
