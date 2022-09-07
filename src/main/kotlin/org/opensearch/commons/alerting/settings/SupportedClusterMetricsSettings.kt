package org.opensearch.commons.alerting.settings

import org.opensearch.commons.alerting.model.ClusterMetricsInput

interface SupportedClusterMetricsSettings {
    fun validateApiType(clusterMetricsInput: ClusterMetricsInput)
}
