/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SendNotificationRequestTests {

    private fun assertGetRequestEquals(
        expected: SendNotificationRequest,
        actual: SendNotificationRequest
    ) {
        assertEquals(expected.eventSource, actual.eventSource)
        assertEquals(expected.channelMessage, actual.channelMessage)
        assertEquals(expected.channelIds, actual.channelIds)
        assertEquals(expected.threadContext, actual.threadContext)
        assertNull(actual.validate())
    }

    @Test
    fun `Send request serialize and deserialize transport object should be equal`() {
        val notificationInfo = EventSource(
            "title",
            "reference_id",
            SeverityType.HIGH,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val recreatedObject = recreateObject(request) { SendNotificationRequest(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request serialize and deserialize using json object should be equal`() {
        val notificationInfo = EventSource(
            "title",
            "reference_id",
            SeverityType.CRITICAL,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should safely ignore extra field in json object`() {
        val notificationInfo = EventSource(
            "title",
            "reference_id",
            SeverityType.HIGH,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val jsonString = """
        {
            "event_source":{
                "title":"${notificationInfo.title}",
                "reference_id":"${notificationInfo.referenceId}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"${channelMessage.textDescription}",
                "html_description":"${channelMessage.htmlDescription}"
            },
            "channel_id_list":["channelId1", "channelId2"],
            "context":"${request.threadContext}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should safely ignore thread context is absent in json object`() {
        val notificationInfo = EventSource(
            "title",
            "reference_id",
            SeverityType.INFO,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            null
        )
        val jsonString = """
        {
            "event_source":{
                "title":"${notificationInfo.title}",
                "reference_id":"${notificationInfo.referenceId}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"${channelMessage.textDescription}",
                "html_description":"${channelMessage.htmlDescription}"
            },
            "channel_id_list":["channelId1", "channelId2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should throw exception if notificationInfo field is absent in json object`() {
        val jsonString = """
        {
            "channel_message":{
                "text_description":"text_description"
            },
            "channel_id_list":["channelId1", "channelId2"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should throw exception if channelMessage field is absent in json object`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_id_list":["channelId1", "channelId2"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should throw exception if channelIds field is absent in json object`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"text_description"
            }
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request validate return exception if channelIds field is empty`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"text_description"
            },
            "channel_id_list":[]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertNotNull(recreatedObject.validate())
    }
}
