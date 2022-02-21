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

internal class ChannelMessageTests {

    @Test
    fun `ChannelMessage Object serialize and deserialize using transport should be equal`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<html>htmlDescription</html>",
            attachment
        )
        val recreatedObject = recreateObject(channelMessage) { ChannelMessage(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Object serialize and deserialize using json should be equal`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<html>htmlDescription</html>",
            attachment
        )
        val jsonString = getJsonString(channelMessage)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Json parsing should safely ignore extra fields`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<html>htmlDescription</html>",
            attachment
        )
        val jsonString = """
        {
            "text_description":"textDescription",
            "html_description":"<html>htmlDescription</html>",
            "attachment":{
                "file_name":"fileName",
                "file_encoding":"fileEncoding",
                "file_data":"fileData",
                "file_content_type":"fileContentType"
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Json parsing should safely ignore missing html description`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            null,
            attachment
        )
        val jsonString = """
        {
            "text_description":"textDescription",
            "attachment":{
                "file_name":"fileName",
                "file_encoding":"fileEncoding",
                "file_data":"fileData",
                "file_content_type":"fileContentType"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Json parsing should safely ignore missing attachment`() {
        val channelMessage = ChannelMessage(
            "textDescription",
            "<html>htmlDescription</html>",
            null
        )
        val jsonString = """
        {
            "text_description":"textDescription",
            "html_description":"<html>htmlDescription</html>"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Json parsing should safely ignore both missing html_description and attachment`() {
        val channelMessage = ChannelMessage(
            "textDescription",
            null,
            null
        )
        val jsonString = """
        {
            "text_description":"textDescription"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        assertEquals(channelMessage, recreatedObject)
    }

    @Test
    fun `ChannelMessage Json parsing should throw exception if text_description is empty`() {
        val jsonString = """
        {
            "text_description":"",
            "html_description":"<html>htmlDescription</html>",
            "attachment":{
                "file_name":"fileName",
                "file_encoding":"fileEncoding",
                "file_data":"fileData",
                "file_content_type":"fileContentType"
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        }
    }

    @Test
    fun `ChannelMessage Json parsing should throw exception if text_description is absent`() {
        val jsonString = """
        {
            "html_description":"<html>htmlDescription</html>",
            "attachment":{
                "file_name":"fileName",
                "file_encoding":"fileEncoding",
                "file_data":"fileData",
                "file_content_type":"fileContentType"
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { ChannelMessage.parse(it) }
        }
    }
}
