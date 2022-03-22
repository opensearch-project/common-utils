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

internal class FilterConfigListTests {

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
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.SLACK
        )
        val searchResult = ChannelList(sampleConfig)
        val recreatedObject = recreateObject(searchResult) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val sampleConfig1 = Channel(
            "config_id1",
            "name1",
            "description1",
            ConfigType.SLACK
        )
        val sampleConfig2 = Channel(
            "config_id2",
            "name2",
            "description2",
            ConfigType.CHIME
        )
        val sampleConfig3 = Channel(
            "config_id3",
            "name3",
            "description3",
            ConfigType.WEBHOOK
        )
        val searchResult = ChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val recreatedObject = recreateObject(searchResult) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json config object should be equal`() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.EMAIL_GROUP
        )
        val searchResult = ChannelList(sampleConfig)
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple config object should be equal`() {
        val sampleConfig1 = Channel(
            "config_id1",
            "name1",
            "description1",
            ConfigType.SLACK
        )
        val sampleConfig2 = Channel(
            "config_id2",
            "name2",
            "description2",
            ConfigType.CHIME
        )
        val sampleConfig3 = Channel(
            "config_id3",
            "name3",
            "description3",
            ConfigType.WEBHOOK
        )
        val searchResult = ChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should use isEnabled=true if absent in json object`() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.EMAIL,
            true
        )
        val searchResult = ChannelList(sampleConfig)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"email"
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.EMAIL
        )
        val searchResult = ChannelList(sampleConfig)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"email",
                    "is_enabled":true
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.EMAIL
        )
        val searchResult = ChannelList(sampleConfig)
        val jsonString = """
        {
            "channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"email",
                    "is_enabled":true
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should throw exception if channelList is absent in json`() {
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
