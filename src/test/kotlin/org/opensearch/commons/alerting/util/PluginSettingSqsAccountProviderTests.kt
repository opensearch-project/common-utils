/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings

class PluginSettingSqsAccountProviderTests {

    private val testSetting: Setting<String> = Setting.simpleString(
        "plugins.alerting.sqs_account_id",
        Setting.Property.NodeScope,
        Setting.Property.Dynamic
    )

    private fun clusterSettings(settings: Settings, vararg s: Setting<*>) =
        ClusterSettings(settings, hashSetOf<Setting<*>>().apply { addAll(s) })

    @Test
    fun `test returns initial setting value as singleton list`() {
        val settings = Settings.builder().put(testSetting.key, "123456789012").build()
        val provider = PluginSettingSqsAccountProvider(testSetting, settings, clusterSettings(settings, testSetting))

        assertEquals(listOf("123456789012"), provider.getAccountIds())
    }

    @Test
    fun `test returns updated value after dynamic setting change`() {
        val settings = Settings.builder().put(testSetting.key, "111111111111").build()
        val cs = clusterSettings(settings, testSetting)
        val provider = PluginSettingSqsAccountProvider(testSetting, settings, cs)

        assertEquals(listOf("111111111111"), provider.getAccountIds())

        cs.applySettings(Settings.builder().put(testSetting.key, "222222222222").build())

        assertEquals(listOf("222222222222"), provider.getAccountIds())
    }

    @Test
    fun `test throws exception when setting not configured`() {
        val settings = Settings.EMPTY
        val provider = PluginSettingSqsAccountProvider(testSetting, settings, clusterSettings(settings, testSetting))

        assertThrows(IllegalArgumentException::class.java) {
            provider.getAccountIds()
        }
    }

    @Test
    fun `test throws exception when setting updated to blank`() {
        val settings = Settings.builder().put(testSetting.key, "123456789012").build()
        val cs = clusterSettings(settings, testSetting)
        val provider = PluginSettingSqsAccountProvider(testSetting, settings, cs)

        cs.applySettings(Settings.builder().put(testSetting.key, "").build())

        assertThrows(IllegalArgumentException::class.java) {
            provider.getAccountIds()
        }
    }

    @Test
    fun `test separate instances use different settings`() {
        val otherSetting: Setting<String> = Setting.simpleString(
            "plugins.ad.sqs_account_id",
            Setting.Property.NodeScope,
            Setting.Property.Dynamic
        )
        val settings = Settings.builder()
            .put(testSetting.key, "account-a")
            .put(otherSetting.key, "account-b")
            .build()
        val cs = clusterSettings(settings, testSetting, otherSetting)

        val providerA = PluginSettingSqsAccountProvider(testSetting, settings, cs)
        val providerB = PluginSettingSqsAccountProvider(otherSetting, settings, cs)

        assertEquals(listOf("account-a"), providerA.getAccountIds())
        assertEquals(listOf("account-b"), providerB.getAccountIds())
    }
}
