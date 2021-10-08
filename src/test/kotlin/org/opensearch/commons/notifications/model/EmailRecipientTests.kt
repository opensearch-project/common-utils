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

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class EmailRecipientTests {

    private fun checkValidEmailAddress(emailAddress: String) {
        assertDoesNotThrow("should accept $emailAddress") {
            EmailRecipient(emailAddress)
        }
    }

    private fun checkInvalidEmailAddress(emailAddress: String) {
        assertThrows<IllegalArgumentException>("Should throw an Exception for invalid email $emailAddress") {
            EmailRecipient(emailAddress)
        }
    }

    @Test
    fun `EmailRecipient should accept valid email address`() {
        checkValidEmailAddress("email1234@email.com")
        checkValidEmailAddress("email+1234@email.com")
        checkValidEmailAddress("email-1234@email.com")
        checkValidEmailAddress("email_1234@email.com")
        checkValidEmailAddress("email.1234@email.com")
        checkValidEmailAddress("e.ma_il-1+2@test-email-domain.co.uk")
        checkValidEmailAddress("email-.+_=#|@domain.com")
        checkValidEmailAddress("e@mail.com")
    }

    @Test
    fun `EmailRecipient should throw exception for invalid email address`() {
        checkInvalidEmailAddress("email")
        checkInvalidEmailAddress("email@")
        checkInvalidEmailAddress("email@1234@email.com")
        checkInvalidEmailAddress(".email@email.com")
        checkInvalidEmailAddress("email.@email.com")
        checkInvalidEmailAddress("email..1234@email.com")
        checkInvalidEmailAddress("email@email..com")
        checkInvalidEmailAddress("email@.com")
        checkInvalidEmailAddress("email@email.com.")
        checkInvalidEmailAddress("email@.email.com")
        checkInvalidEmailAddress("email@email.com-")
        checkInvalidEmailAddress("email@email_domain.com")
    }

    @Test
    fun `EmailRecipient serialize and deserialize transport object should be equal`() {
        val sampleEmailRecipient = EmailRecipient("email1@email.com")
        val recreatedObject = recreateObject(sampleEmailRecipient) { EmailRecipient(it) }
        assertEquals(sampleEmailRecipient, recreatedObject)
    }

    @Test
    fun `EmailRecipient serialize and deserialize using json object should be equal`() {
        val sampleEmailRecipient = EmailRecipient("email1@email.com")
        val jsonString = getJsonString(sampleEmailRecipient)
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipient.parse(it) }
        assertEquals(sampleEmailRecipient, recreatedObject)
    }

    @Test
    fun `EmailRecipient should deserialize json object using parser`() {
        val sampleEmailRecipient = EmailRecipient("email1@email.com")
        val jsonString = """
            {
                "recipient": "${sampleEmailRecipient.recipient}"
            }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipient.parse(it) }
        assertEquals(sampleEmailRecipient, recreatedObject)
    }

    @Test
    fun `EmailRecipient should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { EmailRecipient.parse(it) }
        }
    }

    @Test
    fun `EmailRecipient should throw exception when recipients is replaced with recipients2 in json object`() {
        val sampleEmailRecipient = EmailRecipient("email1@email.com")
        val jsonString = """
            {
                "recipient2": "${sampleEmailRecipient.recipient}"
            }"
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { EmailRecipient.parse(it) }
        }
    }

    @Test
    fun `EmailRecipient should safely ignore extra field in json object`() {
        val sampleEmailRecipient = EmailRecipient("email@email.com")
        val jsonString = """
            {
                "recipient": "${sampleEmailRecipient.recipient}",
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipient.parse(it) }
        assertEquals(sampleEmailRecipient, recreatedObject)
    }
}
