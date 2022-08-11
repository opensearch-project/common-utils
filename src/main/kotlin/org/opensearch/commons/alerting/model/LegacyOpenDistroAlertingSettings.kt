/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.common.settings.Setting
import org.opensearch.common.unit.TimeValue
import java.util.concurrent.TimeUnit

/**
 * Legacy Opendistro settings specific to [AlertingPlugin]. These settings include things like history index max age, request timeout, etc...
 */

class LegacyOpenDistroAlertingSettings {

    companion object {

        val ALERT_HISTORY_ENABLED = Setting.boolSetting(
            "opendistro.alerting.alert_history_enabled",
            true,
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )

        val ALERT_HISTORY_ROLLOVER_PERIOD = Setting.positiveTimeSetting(
            "opendistro.alerting.alert_history_rollover_period",
            TimeValue.timeValueHours(12),
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )

        val ALERT_HISTORY_INDEX_MAX_AGE = Setting.positiveTimeSetting(
            "opendistro.alerting.alert_history_max_age",
            TimeValue(30, TimeUnit.DAYS),
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )

        val ALERT_HISTORY_MAX_DOCS = Setting.longSetting(
            "opendistro.alerting.alert_history_max_docs",
            1000L,
            0L,
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )

        val ALERT_HISTORY_RETENTION_PERIOD = Setting.positiveTimeSetting(
            "opendistro.alerting.alert_history_retention_period",
            TimeValue(60, TimeUnit.DAYS),
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )

        val REQUEST_TIMEOUT = Setting.positiveTimeSetting(
            "opendistro.alerting.request_timeout",
            TimeValue.timeValueSeconds(10),
            Setting.Property.NodeScope, Setting.Property.Dynamic, Setting.Property.Deprecated
        )
    }
}
