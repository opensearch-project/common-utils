/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import java.time.Instant

internal class NotificationConfigInfoTests {

    @Test
    fun `Config info serialize and deserialize with config object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            setOf(FEATURE_REPORTS),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            Instant.now(),
            Instant.now(),
            sampleConfig
        )
        val recreatedObject = recreateObject(configInfo) { NotificationConfigInfo(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            setOf(FEATURE_REPORTS),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig
        )
        val jsonString = getJsonString(configInfo)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            setOf(FEATURE_INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            sampleConfig
        )
        val jsonString = """
        {
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info should throw exception if configId is empty`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            setOf(FEATURE_REPORTS),
            configData = sampleSlack
        )
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfigInfo(
                "",
                Instant.now(),
                Instant.now(),
                sampleConfig
            )
        }
    }

    @Test
    fun `Config info should throw exception if configId is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if lastUpdatedTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "config_id":"config-Id",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if createdTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val jsonString = """
        {
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if notificationConfig is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }
}
