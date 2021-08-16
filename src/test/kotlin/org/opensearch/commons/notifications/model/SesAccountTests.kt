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

package org.opensearch.commons.notifications.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SesAccountTests {

    @Test
    fun `SES should throw exception if empty region`() {
        assertThrows<IllegalArgumentException> {
            SesAccount("", null, "from@domain.com")
        }
        val jsonString = """
        {
            "region":"",
            "from_address":"from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES should throw exception if invalid role ARN`() {
        assertThrows<IllegalArgumentException> {
            SesAccount("us-east-1", "arn:aws:iam:us-east-1:0123456789:role-test", "from@domain.com")
        }
        val jsonString = """
        {
            "region":"us-east-1",
            "role_arn":"arn:aws:iam:us-east-1:0123456789:role-test",
            "from_address":"from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES should throw exception when email id is invalid`() {
        val jsonString = """
        {
            "region":"us-east-1",
            "from_address":".from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES serialize and deserialize transport object should be equal`() {
        val sesAccount = SesAccount("us-east-1", "arn:aws:iam::012345678912:role/iam-test", "from@domain.com")
        val recreatedObject = recreateObject(sesAccount) { SesAccount(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES serialize and deserialize using json object should be equal`() {
        val sesAccount = SesAccount("us-east-1", "arn:aws:iam::012345678912:role/iam-test", "from@domain.com")
        val jsonString = getJsonString(sesAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES serialize and deserialize using json object should be equal with null roleArn`() {
        val sesAccount = SesAccount("us-east-1", null, "from@domain.com")
        val jsonString = getJsonString(sesAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES should deserialize json object using parser`() {
        val sesAccount = SesAccount("us-east-1", "arn:aws:iam::012345678912:role/iam-test", "from@domain.com")
        val jsonString = """
        {
            "region":"${sesAccount.awsRegion}",
            "role_arn":"${sesAccount.roleArn}",
            "from_address":"${sesAccount.fromAddress}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES should deserialize json object will null role_arn using parser`() {
        val sesAccount = SesAccount("us-east-1", null, "from@domain.com")
        val jsonString = """
        {
            "region":"${sesAccount.awsRegion}",
            "role_arn":null,
            "from_address":"${sesAccount.fromAddress}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES should deserialize json object will missing role_arn using parser`() {
        val sesAccount = SesAccount("us-east-1", null, "from@domain.com")
        val jsonString = """
        {
            "region":"${sesAccount.awsRegion}",
            "from_address":"${sesAccount.fromAddress}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }

    @Test
    fun `SES should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES should throw exception when region is replace with region2 in json object`() {
        val jsonString = """
        {
            "region2":"us-east-1",
            "role_arn":"arn:aws:iam::012345678912:role/iam-test",
            "from_address":"from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES should throw exception when from_address is replace with from_address2 in json object`() {
        val jsonString = """
        {
            "region":"us-east-1",
            "role_arn":"arn:aws:iam::012345678912:role/iam-test",
            "from_address2":"from@domain.com"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        }
    }

    @Test
    fun `SES should safely ignore extra field in json object`() {
        val sesAccount = SesAccount("us-east-1", "arn:aws:iam::012345678912:role/iam-test", "from@domain.com")
        val jsonString = """
        {
            "region":"${sesAccount.awsRegion}",
            "role_arn":"${sesAccount.roleArn}",
            "from_address":"${sesAccount.fromAddress}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SesAccount.parse(it) }
        assertEquals(sesAccount, recreatedObject)
    }
}
