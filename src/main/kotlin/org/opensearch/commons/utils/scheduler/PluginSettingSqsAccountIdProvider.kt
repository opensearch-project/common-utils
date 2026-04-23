package org.opensearch.commons.utils.scheduler

import org.opensearch.common.settings.Settings

/**
 * Default [SqsAccountIdProvider] that reads a single account ID from a plugin setting.
 *
 * @param setting the [org.opensearch.common.settings.Setting] to read the account ID from
 * @param settings the node [Settings]
 */
class PluginSettingSqsAccountIdProvider(
    private val setting: org.opensearch.common.settings.Setting<String>,
    private val settings: Settings
) : SqsAccountIdProvider {

    override val type: String = "plugin_setting"

    override fun getAccountIds(): List<String> {
        val accountId = setting.get(settings)
        return if (accountId.isNullOrBlank()) emptyList() else listOf(accountId)
    }
}
