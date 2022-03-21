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

internal class ChannelTests {

    @Test
    fun `Channel Object serialize and deserialize using transport should be equal`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val recreatedObject = recreateObject(channel) { Channel(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Object serialize and deserialize using json should be equal`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.CHIME,
            false
        )
        val jsonString = getJsonString(channel)
        val recreatedObject = createObjectFromJsonString(jsonString) { Channel.parse(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Json parsing should safely ignore extra fields`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            true
        )
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "description":"description",
            "config_type":"email_group",
            "is_enabled":true,
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Channel.parse(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Json parsing should safely ignore unknown config type`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.NONE,
            true
        )
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "description":"description",
            "config_type":"NewConfig",
            "is_enabled":true
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Channel.parse(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Json parsing should safely parse if description is absent`() {
        val channel = Channel(
            "configId",
            "name",
            "",
            ConfigType.SLACK,
            true
        )
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "config_type":"slack",
            "is_enabled":true
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Channel.parse(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Json parsing should safely parse if is_enabled is absent`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "description":"description",
            "config_type":"slack"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Channel.parse(it) }
        assertEquals(channel, recreatedObject)
    }

    @Test
    fun `Channel Json parsing should throw exception if config_id is absent`() {
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "config_type":"slack",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Channel.parse(it) }
        }
    }

    @Test
    fun `Channel Json parsing should throw exception if config_id is empty`() {
        val jsonString = """
        {
            "config_id":"",
            "name":"name",
            "description":"description",
            "config_type":"chime",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Channel.parse(it) }
        }
    }

    @Test
    fun `Channel Json parsing should throw exception if name is absent`() {
        val jsonString = """
        {
            "config_id":"configId",
            "description":"description",
            "config_type":"webhook",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Channel.parse(it) }
        }
    }

    @Test
    fun `Channel Json parsing should throw exception if name is empty`() {
        val jsonString = """
        {
            "config_id":"configId",
            "name":"",
            "description":"description",
            "config_type":"email",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Channel.parse(it) }
        }
    }

    @Test
    fun `Channel Json parsing should throw exception if config_type is absent`() {
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "description":"description",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { Channel.parse(it) }
        }
    }
}
