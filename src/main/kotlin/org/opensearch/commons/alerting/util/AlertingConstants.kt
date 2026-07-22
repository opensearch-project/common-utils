package org.opensearch.commons.alerting.util

class AlertingConstants {
    companion object {
        const val ALL_ALERT_INDEX_PATTERN = ".opendistro-alerting-alert*"

        const val ALL_COMMENTS_INDEX_PATTERN = ".opensearch-alerting-comments*"

        /** Resource type registered with the security plugin's resource-sharing framework. */
        const val MONITOR_RESOURCE_TYPE = "monitor"
    }
}
