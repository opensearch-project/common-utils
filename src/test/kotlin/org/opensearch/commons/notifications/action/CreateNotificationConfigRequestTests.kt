/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipient
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class CreateNotificationConfigRequestTests {

    private fun createWebhookContentConfigObject(): NotificationConfig {
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            configData = sampleWebhook,
            isEnabled = true
        )
    }

    private fun createSlackContentConfigObject(): NotificationConfig {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack,
            isEnabled = true
        )
    }

    private fun createChimeContentConfigObject(): NotificationConfig {
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            configData = sampleChime,
            isEnabled = true
        )
    }
    private fun createMicrosoftTeamsContentConfigObject(): NotificationConfig {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_microsoft_teams_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.MICROSOFT_TEAMS,
            configData = sampleMicrosoftTeams,
            isEnabled = true
        )
    }

    private fun createEmailGroupContentConfigObject(): NotificationConfig {
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("dummy@company.com")))
        return NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            configData = sampleEmailGroup,
            isEnabled = true
        )
    }

    private fun createEmailContentConfigObject(): NotificationConfig {
        val sampleEmail = Email(
            emailAccountID = "sample_1@dummy.com",
            recipients = listOf(EmailRecipient("sample_2@dummy.com")),
            emailGroupIds = listOf("sample_3@dummy.com")
        )
        return NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            configData = sampleEmail,
            isEnabled = true
        )
    }

    private fun createSmtpAccountContentConfigObject(): NotificationConfig {
        val sampleSmtpAccount = SmtpAccount(
            host = "http://dummy.com",
            port = 11,
            method = MethodType.SSL,
            fromAddress = "sample@dummy.com"
        )
        return NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            configData = sampleSmtpAccount,
            isEnabled = true
        )
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal webhook`() {
        val configRequest = CreateNotificationConfigRequest(
            createWebhookContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal microsoft teams`() {
        val configRequest = CreateNotificationConfigRequest(
            createMicrosoftTeamsContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal slack`() {
        val configRequest = CreateNotificationConfigRequest(
            createSlackContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal chime`() {
        val configRequest = CreateNotificationConfigRequest(
            createChimeContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal email`() {
        val configRequest = CreateNotificationConfigRequest(
            createEmailContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal emailGroup`() {
        val configRequest = CreateNotificationConfigRequest(
            createEmailGroupContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize transport object should be equal SmtpAccount`() {
        val configRequest = CreateNotificationConfigRequest(
            createSmtpAccountContentConfigObject()
        )
        val recreatedObject =
            recreateObject(configRequest) {
                CreateNotificationConfigRequest(
                    it
                )
            }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal microsoft teams`() {
        val configRequest = CreateNotificationConfigRequest(
            createMicrosoftTeamsContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal`() {
        val configRequest = CreateNotificationConfigRequest(
            createWebhookContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal slack`() {
        val configRequest = CreateNotificationConfigRequest(
            createSlackContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal chime`() {
        val configRequest = CreateNotificationConfigRequest(
            createChimeContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal email`() {
        val configRequest = CreateNotificationConfigRequest(
            createEmailContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal EmailGroup`() {
        val configRequest = CreateNotificationConfigRequest(
            createEmailGroupContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal SmtpAccount`() {
        val configRequest = CreateNotificationConfigRequest(
            createSmtpAccountContentConfigObject()
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser slack`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack,
            isEnabled = true
        )

        val jsonString = """
        {
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser microsoft teams`() {
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.com/sample_microsoft_teams_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.MICROSOFT_TEAMS,
            configData = sampleMicrosoftTeams,
            isEnabled = true
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"microsoft_teams",
                "is_enabled":true,
                "microsoft_teams":{"url":"https://domain.com/sample_microsoft_teams_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser webhook`() {
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            configData = sampleWebhook,
            isEnabled = true
        )

        val jsonString = """
        {
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"webhook",
                "is_enabled":true,
                "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser Chime`() {
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            configData = sampleChime,
            isEnabled = true
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"chime",
                "is_enabled":true,
                "chime":{"url":"https://domain.com/sample_chime_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser Email Group`() {
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("dummy@company.com")))
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            configData = sampleEmailGroup,
            isEnabled = true
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"email_group",
                "is_enabled":true,
                "email_group":{"recipient_list":[{"recipient":"dummy@company.com"}]}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Update config should deserialize json object using parser Email`() {
        val sampleEmail = Email(
            emailAccountID = "sample_1@dummy.com",
            recipients = listOf(EmailRecipient("sample_2@dummy.com")),
            emailGroupIds = listOf("sample_3@dummy.com")
        )
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            configData = sampleEmail,
            isEnabled = true
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"email",
                "is_enabled":true,
                "email":{
                    "email_account_id":"sample_1@dummy.com",
                    "recipient_list":[{"recipient":"sample_2@dummy.com"}],
                    "email_group_id_list":["sample_3@dummy.com"]
                }
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Update config should deserialize json object using parser SmtpAccount`() {
        val sampleSmtpAccount = SmtpAccount(
            host = "http://dummy.com",
            port = 11,
            method = MethodType.SSL,
            fromAddress = "sample@dummy.com"
        )
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            configData = sampleSmtpAccount,
            isEnabled = true
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"smtp_account",
                "is_enabled":true,
                "smtp_account":{"host":"http://dummy.com", "port":11,"method": "ssl", "from_address": "sample@dummy.com" }
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        }
    }

    private fun validateSpecialCharsInIdFails(char: Char) {
        val str = when (char) {
            '"' -> "\\\""
            '\\' -> "\\\\"
            else -> "$char"
        }
        val jsonString = """
        {
            "config_id":"config_id1$str",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"chime",
                "is_enabled":true,
                "chime":{"url":"https://domain.com/sample_chime_url#1234567890"}
            }
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException>("Should not accept char:$char") {
            createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        }
    }

    @Test
    fun `Create config with special char in config_id should fail`() {
        "`~!@#$%^&*()=[]{}|:;'<>,.?\\\"".forEach {
            validateSpecialCharsInIdFails(it)
        }
    }

    @Test
    fun `Create config should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack,
            isEnabled = true
        )

        val jsonString = """
        {
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }
}
