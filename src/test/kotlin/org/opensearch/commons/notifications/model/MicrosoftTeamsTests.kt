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

internal class MicrosoftTeamsTests {

    @Test
    fun `Microsoft Teams serialize and deserialize transport object should be equal`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        val recreatedObject = recreateObject(sampleMicrosoftTeams) { MicrosoftTeams(it) }
        assertEquals(sampleMicrosoftTeams, recreatedObject)
    }

    @Test
    fun `Microsoft Teams serialize and deserialize using json object should be equal`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleMicrosoftTeams)
        val recreatedObject = createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        assertEquals(sampleMicrosoftTeams, recreatedObject)
    }

    @Test
    fun `Microsoft Teams should deserialize json object using parser`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleMicrosoftTeams.url}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        assertEquals(sampleMicrosoftTeams, recreatedObject)
    }

    @Test
    fun `Microsoft Teams should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        }
    }

    @Test
    fun `Microsoft Teams should throw exception when url is replace with url2 in json object`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url2\":\"${sampleMicrosoftTeams.url}\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        }
    }

    @Test
    fun `Microsoft Teams should throw exception when url is not proper`() {
        assertThrows<MalformedURLException> {
            MicrosoftTeams("domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"domain.com/sample_url\"}"
        assertThrows<MalformedURLException> {
            createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        }
    }

    @Test
    fun `Microsoft Teams should throw exception when url protocol is not https or http`() {
        assertThrows<IllegalArgumentException> {
            MicrosoftTeams("ftp://domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"ftp://domain.com/sample_url\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        }
    }

    @Test
    fun `Microsoft Teams should safely ignore extra field in json object`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleMicrosoftTeams.url}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { MicrosoftTeams.parse(it) }
        assertEquals(sampleMicrosoftTeams, recreatedObject)
    }
}
