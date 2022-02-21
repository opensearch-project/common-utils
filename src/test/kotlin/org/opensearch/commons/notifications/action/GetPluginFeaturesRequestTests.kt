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

internal class GetPluginFeaturesRequestTests {

    private fun assertGetRequestEquals(
        expected: GetPluginFeaturesRequest,
        actual: GetPluginFeaturesRequest
    ) {
        assertEquals(expected.compact, actual.compact)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val request = GetPluginFeaturesRequest()
        val recreatedObject = recreateObject(request) { GetPluginFeaturesRequest(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val request = GetPluginFeaturesRequest()
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val request = GetPluginFeaturesRequest()
        val jsonString = """
        {
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }
}
