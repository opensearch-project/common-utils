/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class ChannelListTests {

    private fun assertSearchResultEquals(
        expected: ChannelList,
        actual: ChannelList
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using transport should be equal`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val channelList = ChannelList(channel)
        val recreatedObject = recreateObject(channelList) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize multiple object with default values should be equal`() {
        val channel1 = Channel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val channel2 = Channel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val channelList = ChannelList(listOf(channel1, channel2))
        val expectedResult = ChannelList(
            0,
            2,
            TotalHits.Relation.EQUAL_TO,
            listOf(channel1, channel2)
        )
        val recreatedObject = recreateObject(channelList) { ChannelList(it) }
        assertSearchResultEquals(expectedResult, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize with multiple object should be equal`() {
        val channel1 = Channel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val channel2 = Channel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val channelList = ChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(channel1, channel2)
        )
        val recreatedObject = recreateObject(channelList) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using json should be equal`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val channelList = ChannelList(channel)
        val jsonString = getJsonString(channelList)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List serialize and deserialize using json with multiple object should be equal`() {
        val channel1 = Channel(
            "configId1",
            "name1",
            "description1",
            ConfigType.SLACK,
            true
        )
        val channel2 = Channel(
            "configId2",
            "name2",
            "description2",
            ConfigType.CHIME,
            true
        )
        val channelList = ChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(channel1, channel2)
        )
        val jsonString = getJsonString(channelList)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List should safely ignore extra field in json object`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val channelList = ChannelList(channel)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "channel_list":[
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
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Feature Channel List should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val channel = Channel(
            "configId",
            "name",
            "description",
            ConfigType.SLACK,
            true
        )
        val channelList = ChannelList(channel)
        val jsonString = """
        {
            "channel_list":[
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
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(channelList, recreatedObject)
    }

    @Test
    fun `Channel List should throw exception if channel_list is absent in json`() {
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { ChannelList(it) }
        }
    }
}
