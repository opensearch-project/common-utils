package org.opensearch.commons.alerting.util

import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings

/**
 * [SqsAccountProvider] implementation that reads a single account ID from a plugin setting
 * and stays in sync with dynamic setting updates.
 *
 * @param setting the plugin setting that holds the SQS account ID
 * @param settings the node settings to read the initial value from
 * @param clusterSettings the cluster settings to register an update consumer on
 */
class PluginSettingSqsAccountProvider(
    setting: Setting<String>,
    settings: Settings,
    clusterSettings: ClusterSettings
) : SqsAccountProvider {

    private val settingKey: String = setting.key

    @Volatile
    private var accountId: String = setting.get(settings)

    init {
        clusterSettings.addSettingsUpdateConsumer(setting) { accountId = it }
    }

    override fun getAccountIds(): List<String> {
        require(accountId.isNotBlank()) { "Setting [$settingKey] must be defined" }
        return listOf(accountId)
    }
}
