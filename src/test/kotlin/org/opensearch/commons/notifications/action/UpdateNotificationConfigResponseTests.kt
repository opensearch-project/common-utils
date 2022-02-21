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

internal class UpdateNotificationConfigResponseTests {

    @Test
    fun `Update response serialize and deserialize transport object should be equal`() {
        val configResponse = UpdateNotificationConfigResponse("sample_config_id")
        val recreatedObject = recreateObject(configResponse) { UpdateNotificationConfigResponse(it) }
        assertEquals(configResponse.configId, recreatedObject.configId)
    }

    @Test
    fun `Update response serialize and deserialize using json object should be equal`() {
        val configResponse = UpdateNotificationConfigResponse("sample_config_id")
        val jsonString = getJsonString(configResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigResponse.parse(it) }
        assertEquals(configResponse.configId, recreatedObject.configId)
    }

    @Test
    fun `Update response should deserialize json object using parser`() {
        val configId = "sample_config_id"
        val jsonString = "{\"config_id\":\"$configId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigResponse.parse(it) }
        assertEquals(configId, recreatedObject.configId)
    }

    @Test
    fun `Update response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should throw exception when configId is replace with configId2 in json object`() {
        val jsonString = "{\"config_id2\":\"sample_config_id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { UpdateNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should safely ignore extra field in json object`() {
        val configId = "sample_config_id"
        val jsonString = """
        {
            "config_id":"$configId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigResponse.parse(it) }
        assertEquals(configId, recreatedObject.configId)
    }
}
