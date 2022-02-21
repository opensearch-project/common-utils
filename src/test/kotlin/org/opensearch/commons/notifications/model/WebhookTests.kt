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

internal class WebhookTests {

    @Test
    fun `Webhook serialize and deserialize transport object should be equal`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890", mapOf(Pair("key", "value")))
        val recreatedObject = recreateObject(sampleWebhook) { Webhook(it) }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Webhook serialize and deserialize using json object should be equal`() {
        val sampleWebhook = Webhook(
            "http://domain.com/sample_url#1234567890",
            mapOf(Pair("key", "value")),
            HttpMethodType.PUT
        )
        val jsonString = getJsonString(sampleWebhook)
        val recreatedObject = createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Webhook should deserialize json object using parser`() {
        val sampleWebhook = Webhook(
            "https://domain.com/sample_url#1234567890",
            mapOf(Pair("key", "value")),
            HttpMethodType.PATCH
        )
        val jsonString = """
            {
                "url":"${sampleWebhook.url}",
                "header_params":{
                    "key":"value"
                },
                "method":"PATCH"
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Webhook should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        }
    }

    @Test
    fun `Webhook should throw exception when url is replace with url2 in json object`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url2\":\"${sampleWebhook.url}\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        }
    }

    @Test
    fun `Webhook should throw exception when url is not proper`() {
        assertThrows<MalformedURLException> {
            Webhook("domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"domain.com/sample_url\"}"
        assertThrows<MalformedURLException> {
            createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        }
    }

    @Test
    fun `Webhook should throw exception when url protocol is not https or http`() {
        assertThrows<IllegalArgumentException> {
            Webhook("ftp://domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"ftp://domain.com/sample_url\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        }
    }

    @Test
    fun `Webhook should safely ignore extra field in json object`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleWebhook.url}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Webhook.parse(it) }
        assertEquals(sampleWebhook, recreatedObject)
    }
}
