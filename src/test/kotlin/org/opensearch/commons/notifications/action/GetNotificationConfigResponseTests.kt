/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import java.time.Instant

internal class GetNotificationConfigResponseTests {

    private fun assertSearchResultEquals(
        expected: NotificationConfigSearchResult,
        actual: NotificationConfigSearchResult
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            Instant.now(),
            Instant.now(),
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val searchResponse = GetNotificationConfigResponse(searchResult)
        val recreatedObject = recreateObject(searchResponse) { GetNotificationConfigResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val sampleConfig1 = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = Slack("https://domain.com/sample_url#1234567890")
        )
        val configInfo1 = NotificationConfigInfo(
            "config_id1",
            Instant.now(),
            Instant.now(),
            sampleConfig1
        )
        val sampleConfig2 = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            configData = Chime("https://domain.com/sample_url#1234567890")
        )
        val configInfo2 = NotificationConfigInfo(
            "config_id2",
            Instant.now(),
            Instant.now(),
            sampleConfig2
        )
        val sampleConfig3 = NotificationConfig(
            "name",
            "description",
            ConfigType.MICROSOFT_TEAMS,
            configData = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        )
        val configInfo3 = NotificationConfigInfo(
            "config_id3",
            Instant.now(),
            Instant.now(),
            sampleConfig3
        )
        val searchResult = NotificationConfigSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(configInfo1, configInfo2, configInfo3)
        )
        val searchResponse = GetNotificationConfigResponse(searchResult)
        val recreatedObject = recreateObject(searchResponse) { GetNotificationConfigResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val searchResponse = GetNotificationConfigResponse(searchResult)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleConfig1 = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = Slack("https://domain.com/sample_url#1234567890")
        )
        val configInfo1 = NotificationConfigInfo(
            "config_id1",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig1
        )
        val sampleConfig2 = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            configData = Chime("https://domain.com/sample_url#1234567890")
        )
        val configInfo2 = NotificationConfigInfo(
            "config_id2",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig2
        )
        val sampleConfig3 = NotificationConfig(
            "name",
            "description",
            ConfigType.MICROSOFT_TEAMS,
            configData = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        )
        val configInfo3 = NotificationConfigInfo(
            "config_id3",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig3
        )
        val searchResult = NotificationConfigSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(configInfo1, configInfo2, configInfo3)
        )
        val searchResponse = GetNotificationConfigResponse(searchResult)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack,
            isEnabled = true
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "config_list":[
                {
                    "config_id":"config-Id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                    "config":{
                        "name":"name",
                        "description":"description",
                        "config_type":"slack",
                        "is_enabled":true,
                        "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
                    }
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack,
            isEnabled = true
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val jsonString = """
        {
            "config_list":[
                {
                    "config_id":"config-Id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                    "config":{
                        "name":"name",
                        "description":"description",
                        "config_type":"slack",
                        "is_enabled":true,
                        "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
                    }
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should throw exception if notificationConfigs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "config_list":[
                {
                    "config_id":"config-Id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}"
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetNotificationConfigResponse.parse(it) }
        }
    }
}
