package org.opensearch.commons.alerting.util

class AlertingConstants {
    companion object {
        const val ALL_ALERT_INDEX_PATTERN = ".opendistro-alerting-alert*"

        const val ALL_COMMENTS_INDEX_PATTERN = ".opensearch-alerting-comments*"

        /** Resource type registered with the security plugin's resource-sharing framework for monitors. */
        const val MONITOR_RESOURCE_TYPE = "monitor"

        /**
         * Resource type registered with the security plugin's resource-sharing framework for workflows.
         * The value is "alerting-workflow" (not "workflow") to avoid colliding with the "workflow"
         * resource type registered by flow-framework. Alerting registers the same value on its
         * [org.opensearch.security.spi.resources.ResourceProvider], so this constant must match.
         */
        const val WORKFLOW_RESOURCE_TYPE = "alerting-workflow"
    }
}
