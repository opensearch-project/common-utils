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

internal class SmtpAccountTests {

    @Test
    fun `SmtpAccount serialize and deserialize transport object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, MethodType.SSL, "from@domain.com")
        val recreatedObject = recreateObject(sampleSmtpAccount) { SmtpAccount(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount serialize and deserialize using json object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, MethodType.SSL, "from@domain.com")
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount should deserialize json object using parser`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, MethodType.SSL, "from@domain.com")
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"ssl",
            "from_address":"from@domain.com"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        }
    }

    @Test
    fun `SmtpAccount should throw exception when email id is invalid`() {
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"ssl",
            "from_address":".from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        }
    }

    @Test
    fun `SmtpAccount should safely ignore extra field in json object`() {
        val sampleSmtpAccount = SmtpAccount(
            "domain.com",
            1234, MethodType.START_TLS,
            "from@domain.com"
        )
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"start_tls",
            "from_address":"from@domain.com",
            "extra_field_1":"extra value 1",
            "extra_field_2":"extra value 2"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }
}
