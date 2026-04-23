/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils.scheduler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings

class PluginSettingSqsAccountIdProviderTests {

    private val testSetting: Setting<String> = Setting.simpleString(
        "plugins.alerting.sqs_account_id",
        Setting.Property.NodeScope,
        Setting.Property.Dynamic
    )

    @Test
    fun `type returns plugin_setting`() {
        val provider = PluginSettingSqsAccountIdProvider(testSetting, Settings.EMPTY)
        assertEquals("plugin_setting", provider.type)
    }

    @Test
    fun `getAccountIds returns singleton list when setting has value`() {
        val settings = Settings.builder().put(testSetting.key, "123456789012").build()
        val provider = PluginSettingSqsAccountIdProvider(testSetting, settings)
        assertEquals(listOf("123456789012"), provider.getAccountIds())
    }

    @Test
    fun `getAccountIds returns empty list when setting is empty`() {
        val provider = PluginSettingSqsAccountIdProvider(testSetting, Settings.EMPTY)
        assertEquals(emptyList<String>(), provider.getAccountIds())
    }

    @Test
    fun `getAccountIds returns empty list when setting is blank`() {
        val settings = Settings.builder().put(testSetting.key, "   ").build()
        val provider = PluginSettingSqsAccountIdProvider(testSetting, settings)
        assertEquals(emptyList<String>(), provider.getAccountIds())
    }
}
