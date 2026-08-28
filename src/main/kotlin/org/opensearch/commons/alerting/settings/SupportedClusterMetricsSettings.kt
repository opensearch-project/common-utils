/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.settings

import org.opensearch.commons.alerting.model.ClusterMetricsInput

interface SupportedClusterMetricsSettings {
    fun validateApiType(clusterMetricsInput: ClusterMetricsInput)
}
