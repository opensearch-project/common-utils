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

internal class FeatureChannelTests {

    @Test
    fun `FeatureChannel Object serialize and deserialize using transport should be equal`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val recreatedObject = recreateObject(featureChannel) { FeatureChannel(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Object serialize and deserialize using json should be equal`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.CHIME,
            false
        )
        val jsonString = getJsonString(featureChannel)
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Json parsing should safely ignore extra fields`() {
        val featureChannel = FeatureChannel(
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
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Json parsing should safely ignore unknown config type`() {
        val featureChannel = FeatureChannel(
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
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Json parsing should safely parse if description is absent`() {
        val featureChannel = FeatureChannel(
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
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Json parsing should safely parse if is_enabled is absent`() {
        val featureChannel = FeatureChannel(
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
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        assertEquals(featureChannel, recreatedObject)
    }

    @Test
    fun `FeatureChannel Json parsing should throw exception if config_id is absent`() {
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "config_type":"slack",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        }
    }

    @Test
    fun `FeatureChannel Json parsing should throw exception if config_id is empty`() {
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
            createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        }
    }

    @Test
    fun `FeatureChannel Json parsing should throw exception if name is absent`() {
        val jsonString = """
        {
            "config_id":"configId",
            "description":"description",
            "config_type":"webhook",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        }
    }

    @Test
    fun `FeatureChannel Json parsing should throw exception if name is empty`() {
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
            createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        }
    }

    @Test
    fun `FeatureChannel Json parsing should throw exception if config_type is absent`() {
        val jsonString = """
        {
            "config_id":"configId",
            "name":"name",
            "description":"description",
            "is_enabled":true
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannel.parse(it) }
        }
    }
}
