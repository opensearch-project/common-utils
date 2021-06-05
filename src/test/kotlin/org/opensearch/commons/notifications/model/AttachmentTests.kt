/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
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
