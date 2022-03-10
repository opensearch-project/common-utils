/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class DeliveryStatusTests {
    @Test
    fun `DeliveryStatus serialize and deserialize should be equal`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val recreatedObject = recreateObject(sampleDeliveryStatus) { DeliveryStatus(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }

    @Test
    fun `DeliveryStatus serialize and deserialize using json should be equal`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val jsonString = getJsonString(sampleDeliveryStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }

    @Test
    fun `DeliveryStatus should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        }
    }

    @Test
    fun `DeliveryStatus should safely ignore extra field in json object`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val jsonString = """
        {
            "status_code": "404",
            "status_text": "invalid recipient",
            "extra": "field"
        }    
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }
}
