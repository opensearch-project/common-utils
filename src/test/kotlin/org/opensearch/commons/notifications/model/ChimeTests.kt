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

internal class ChimeTests {

    @Test
    fun `Chime serialize and deserialize transport object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val recreatedObject = recreateObject(sampleChime) { Chime(it) }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Chime serialize and deserialize using json object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleChime)
        val recreatedObject = createObjectFromJsonString(jsonString) { Chime.parse(it) }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Chime should deserialize json object using parser`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleChime.url}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Chime.parse(it) }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Chime should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Chime.parse(it) }
        }
    }

    @Test
    fun `Chime should throw exception when url is replace with url2 in json object`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url2\":\"${sampleChime.url}\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Chime.parse(it) }
        }
    }

    @Test
    fun `Chime should throw exception when url is not proper`() {
        assertThrows<MalformedURLException> {
            Chime("domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"domain.com/sample_url\"}"
        assertThrows<MalformedURLException> {
            createObjectFromJsonString(jsonString) { Chime.parse(it) }
        }
    }

    @Test
    fun `Chime should throw exception when url protocol is not https or http`() {
        assertThrows<IllegalArgumentException> {
            Chime("ftp://domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"ftp://domain.com/sample_url\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Chime.parse(it) }
        }
    }

    @Test
    fun `Chime should safely ignore extra field in json object`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleChime.url}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Chime.parse(it) }
        assertEquals(sampleChime, recreatedObject)
    }
}
