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
import org.opensearch.rest.RestStatus

internal class DeleteNotificationConfigResponseTests {

    @Test
    fun `Delete response serialize and deserialize transport object should be equal`() {
        val configResponse = DeleteNotificationConfigResponse(mapOf(Pair("sample_config_id", RestStatus.OK)))
        val recreatedObject = recreateObject(configResponse) { DeleteNotificationConfigResponse(it) }
        assertEquals(configResponse.configIdToStatus, recreatedObject.configIdToStatus)
    }

    @Test
    fun `Delete response serialize and deserialize using json object should be equal`() {
        val configResponse = DeleteNotificationConfigResponse(mapOf(Pair("sample_config_id", RestStatus.OK)))
        val jsonString = getJsonString(configResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configResponse.configIdToStatus, recreatedObject.configIdToStatus)
    }

    @Test
    fun `Delete response should deserialize json object using parser`() {
        val configId = "sample_config_id"
        val configResponse = DeleteNotificationConfigResponse(mapOf(Pair(configId, RestStatus.OK)))
        val jsonString = """
        {
            "delete_response_list":{
                "$configId":"OK"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configResponse.configIdToStatus, recreatedObject.configIdToStatus)
    }

    @Test
    fun `Delete response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should throw exception when configId is replace with configId2 in json object`() {
        val jsonString = "{\"config_id2\":\"sample_config_id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should safely ignore extra field in json object`() {
        val configId = "sample_config_id"
        val configResponse = DeleteNotificationConfigResponse(mapOf(Pair(configId, RestStatus.OK)))
        val jsonString = """
        {
            "delete_response_list":{
                "$configId":"OK"
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configResponse.configIdToStatus, recreatedObject.configIdToStatus)
    }
}
