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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SNSTests {

    @Test
    fun `SNS should throw exception if empty topic`() {
        assertThrows(IllegalArgumentException::class.java) {
            SNS("", null)
        }
        val jsonString = "{\"topic_arn\":\"\"}"
        assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { SNS.parse(it) }
        }
    }

    @Test
    fun `SNS should throw exception if invalid topic ARN`() {
        assertThrows(IllegalArgumentException::class.java) {
            SNS("arn:aws:es:us-east-1:012345678989:test", null)
        }
        val jsonString = "{\"topic_arn\":\"arn:aws:es:us-east-1:012345678989:test\"}"
        assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { SNS.parse(it) }
        }
    }

    @Test
    fun `SNS should throw exception if invalid role ARN`() {
        assertThrows(IllegalArgumentException::class.java) {
            SNS("arn:aws:sns:us-east-1:012345678912:topic-test", "arn:aws:iam:us-east-1:0123456789:role-test")
        }
        val jsonString =
            "{\"topic_arn\":\"arn:aws:sns:us-east-1:012345678912:topic-test\",\"role_arn\":\"arn:aws:iam:us-east-1:0123456789:role-test\"}"
        assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { SNS.parse(it) }
        }
    }

    @Test
    fun `SNS serialize and deserialize transport object should be equal`() {
        val sampleSNS = SNS("arn:aws:sns:us-east-1:012345678912:topic-test", "arn:aws:iam::012345678912:role/iam-test")
        val recreatedObject = recreateObject(sampleSNS) { SNS(it) }
        Assertions.assertEquals(sampleSNS, recreatedObject)
    }

    @Test
    fun `SNS serialize and deserialize using json object should be equal`() {
        val sampleSNS = SNS("arn:aws:sns:us-east-1:012345678912:topic-test", "arn:aws:iam::012345678912:role/iam-test")
        val jsonString = getJsonString(sampleSNS)
        val recreatedObject = createObjectFromJsonString(jsonString) { SNS.parse(it) }
        Assertions.assertEquals(sampleSNS, recreatedObject)
    }

    @Test
    fun `SNS should deserialize json object using parser`() {
        val sampleSNS = SNS("arn:aws:sns:us-east-1:012345678912:topic-test", "arn:aws:iam::012345678912:role/iam-test")
        val jsonString = "{\"topic_arn\":\"${sampleSNS.topicARN}\",\"role_arn\":\"${sampleSNS.roleARN}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SNS.parse(it) }
        Assertions.assertEquals(sampleSNS, recreatedObject)
    }

    @Test
    fun `SNS should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows(JsonParseException::class.java) {
            createObjectFromJsonString(jsonString) { SNS.parse(it) }
        }
    }

    @Test
    fun `SNS should throw exception when arn is replace with arn2 in json object`() {
        val sampleSNS = SNS("arn:aws:sns:us-east-1:012345678912:topic-test", "arn:aws:iam::012345678912:role/iam-test")
        val jsonString = "{\"topic_arn2\":\"${sampleSNS.topicARN}\",\"role_arn\":\"${sampleSNS.roleARN}\"}"
        assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { SNS.parse(it) }
        }
    }

    @Test
    fun `SNS should safely ignore extra field in json object`() {
        val sampleSNS = SNS("arn:aws:sns:us-east-1:012345678912:topic-test", null)
        val jsonString = "{\"topic_arn\":\"${sampleSNS.topicARN}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SNS.parse(it) }
        Assertions.assertEquals(sampleSNS, recreatedObject)
    }
}
