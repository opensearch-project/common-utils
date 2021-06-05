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

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class FeatureChannelListTests {

    private fun assertSearchResultEquals(
        expected: FeatureChannelList,
        actual: FeatureChannelList
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using transport should be equal`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val featureChannelList = FeatureChannelList(featureChannel)
        val recreatedObject = recreateObject(featureChannelList) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize multiple object with default values should be equal`() {
        val featureChannel1 = FeatureChannel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val featureChannel2 = FeatureChannel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val featureChannelList = FeatureChannelList(listOf(featureChannel1, featureChannel2))
        val expectedResult = FeatureChannelList(
            0,
            2,
            TotalHits.Relation.EQUAL_TO,
            listOf(featureChannel1, featureChannel2)
        )
        val recreatedObject = recreateObject(featureChannelList) { FeatureChannelList(it) }
        assertSearchResultEquals(expectedResult, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize with multiple object should be equal`() {
        val featureChannel1 = FeatureChannel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val featureChannel2 = FeatureChannel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val featureChannelList = FeatureChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(featureChannel1, featureChannel2)
        )
        val recreatedObject = recreateObject(featureChannelList) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using json should be equal`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val featureChannelList = FeatureChannelList(featureChannel)
        val jsonString = getJsonString(featureChannelList)
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using json with multiple object should be equal`() {
        val featureChannel1 = FeatureChannel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val featureChannel2 = FeatureChannel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val featureChannelList = FeatureChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(featureChannel1, featureChannel2)
        )
        val jsonString = getJsonString(featureChannelList)
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List should safely ignore extra field in json object`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val featureChannelList = FeatureChannelList(featureChannel)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "feature_channel_list":[
                {
                    "config_id":"configId",
                    "name":"name",
                    "description":"description",
                    "config_type":"slack",
                    "is_enabled":true
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val featureChannel = FeatureChannel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val featureChannelList = FeatureChannelList(featureChannel)
        val jsonString = """
        {
            "feature_channel_list":[
                {
                    "config_id":"configId",
                    "name":"name",
                    "description":"description",
                    "config_type":"slack",
                    "is_enabled":true
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(featureChannelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List should throw exception if feature_channel_list is absent in json`() {
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        }
    }
}
