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

internal class AttachmentTests {

    @Test
    fun `Attachment Object serialize and deserialize using transport should be equal`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val recreatedObject = recreateObject(attachment) { Attachment(it) }
        assertEquals(attachment, recreatedObject)
    }

    @Test
    fun `Attachment Object serialize and deserialize using json should be equal`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val jsonString = getJsonString(attachment)
        val recreatedObject = createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        assertEquals(attachment, recreatedObject)
    }

    @Test
    fun `Attachment Json parsing should safely ignore extra fields`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            "fileContentType"
        )
        val jsonString = """
        {
            "file_name":"fileName",
            "file_encoding":"fileEncoding",
            "file_data":"fileData",
            "file_content_type":"fileContentType",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        assertEquals(attachment, recreatedObject)
    }

    @Test
    fun `Attachment Json parsing should safely ignore null content type`() {
        val attachment = Attachment(
            "fileName",
            "fileEncoding",
            "fileData",
            null
        )
        val jsonString = """
        {
            "file_name":"fileName",
            "file_encoding":"fileEncoding",
            "file_data":"fileData"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        assertEquals(attachment, recreatedObject)
    }

    @Test
    fun `Attachment Json parsing should throw exception if file_name is absent`() {
        val jsonString = """
        {
            "file_encoding":"fileEncoding",
            "file_data":"fileData",
            "file_content_type":"fileContentType"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        }
    }

    @Test
    fun `Attachment Json parsing should throw exception if file_encoding is absent`() {
        val jsonString = """
        {
            "file_name":"fileName",
            "file_data":"fileData",
            "file_content_type":"fileContentType"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        }
    }

    @Test
    fun `Attachment Json parsing should throw exception if file_data is absent`() {
        val jsonString = """
        {
            "file_name":"fileName",
            "file_encoding":"fileEncoding",
            "file_content_type":"fileContentType"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Attachment.parse(it) }
        }
    }
}
