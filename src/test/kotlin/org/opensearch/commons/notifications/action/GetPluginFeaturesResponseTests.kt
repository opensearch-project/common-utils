/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class GetPluginFeaturesResponseTests {

    private fun assertResponseEquals(
        expected: GetPluginFeaturesResponse,
        actual: GetPluginFeaturesResponse
    ) {
        assertEquals(expected.allowedConfigTypeList, actual.allowedConfigTypeList)
        assertEquals(expected.allowedConfigFeatureList, actual.allowedConfigFeatureList)
        assertEquals(expected.pluginFeatures, actual.pluginFeatures)
    }

    @Test
    fun `Get Response serialize and deserialize transport object should be equal`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            listOf("config_feature_1", "config_feature_2", "config_feature_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val recreatedObject = recreateObject(response) { GetPluginFeaturesResponse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response serialize and deserialize using json config object should be equal`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            listOf("config_feature_1", "config_feature_2", "config_feature_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response should safely ignore extra field in json object`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            listOf("config_feature_1", "config_feature_2", "config_feature_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val jsonString = """
        {
            "allowed_config_type_list":["config_type_1", "config_type_2", "config_type_3"],
            "allowed_config_feature_list":["config_feature_1", "config_feature_2", "config_feature_3"],
            "plugin_features":{
                "FeatureKey1":"FeatureValue1",
                "FeatureKey2":"FeatureValue2",
                "FeatureKey3":"FeatureValue3"
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response should throw exception if allowed_config_type_list is absent in json`() {
        val jsonString = """
        {
            "allowed_config_feature_list":["config_feature_1", "config_feature_2", "config_feature_3"],
            "plugin_features":{
                "FeatureKey1":"FeatureValue1",
                "FeatureKey2":"FeatureValue2",
                "FeatureKey3":"FeatureValue3"
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        }
    }

    @Test
    fun `Get Response should throw exception if allowed_config_feature_list is absent in json`() {
        val jsonString = """
        {
            "allowed_config_type_list":["config_type_1", "config_type_2", "config_type_3"],
            "plugin_features":{
                "FeatureKey1":"FeatureValue1",
                "FeatureKey2":"FeatureValue2",
                "FeatureKey3":"FeatureValue3"
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        }
    }

    @Test
    fun `Get Response should throw exception if plugin_features is absent in json`() {
        val jsonString = """
        {
            "config_type_list":["config_type_1", "config_type_2", "config_type_3"],
            "allowed_config_feature_list":["config_feature_1", "config_feature_2", "config_feature_3"]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        }
    }
}
