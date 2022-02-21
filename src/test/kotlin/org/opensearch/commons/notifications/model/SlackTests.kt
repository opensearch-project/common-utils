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
import java.net.MalformedURLException

internal class SlackTests {

    @Test
    fun `Slack serialize and deserialize transport object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val recreatedObject = recreateObject(sampleSlack) { Slack(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack serialize and deserialize using json object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleSlack)
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack should deserialize json object using parser`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleSlack.url}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url is replace with url2 in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url2\":\"${sampleSlack.url}\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url is not proper`() {
        assertThrows<MalformedURLException> {
            Slack("domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"domain.com/sample_url\"}"
        assertThrows<MalformedURLException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url protocol is not https or http`() {
        assertThrows<IllegalArgumentException> {
            Slack("ftp://domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"ftp://domain.com/sample_url\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleSlack.url}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }
}
