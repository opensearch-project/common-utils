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

internal class GetFeatureChannelListRequestTests {

    private fun assertGetRequestEquals(
        expected: GetFeatureChannelListRequest,
        actual: GetFeatureChannelListRequest
    ) {
        assertEquals(expected.compact, actual.compact)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val configRequest = GetFeatureChannelListRequest()
        val recreatedObject = recreateObject(configRequest) { GetFeatureChannelListRequest(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val configRequest = GetFeatureChannelListRequest()
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureChannelListRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetFeatureChannelListRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val configRequest = GetFeatureChannelListRequest()
        val jsonString = """
        {
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureChannelListRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }
}
