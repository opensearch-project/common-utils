/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_ALERTING
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class EventSourceTests {

    @Test
    fun `Event source serialize and deserialize should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            severity = SeverityType.INFO
        )
        val recreatedObject = recreateObject(sampleEventSource) { EventSource(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source serialize and deserialize using json should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            severity = SeverityType.INFO
        )

        val jsonString = getJsonString(sampleEventSource)
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source should safely ignore extra field in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val jsonString = """
        { 
            "title":"title",
            "reference_id":"reference_id",
            "feature":"alerting",
            "severity":"info",
            "tags":["tag1", "tag2"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source should safely accepts unknown feature type in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            "NewFeature",
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val jsonString = """
        {
            "title":"title",
            "reference_id":"reference_id",
            "feature": "NewFeature",
            "severity":"info",
            "tags":["tag1", "tag2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source throw exception if name is empty`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            EventSource(
                "",
                "reference_id",
                FEATURE_ALERTING,
                tags = listOf("tag1", "tag2"),
                severity = SeverityType.INFO
            )
        }
    }
}
