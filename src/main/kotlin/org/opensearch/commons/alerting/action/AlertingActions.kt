/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionType

object AlertingActions {
    const val INDEX_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/write"
    const val GET_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/get"

    val INDEX_MONITOR_ACTION_TYPE =
        ActionType(INDEX_MONITOR_ACTION_NAME, ::IndexMonitorResponse)

    val GET_FINDINGS_ACTION_TYPE =
        ActionType(GET_FINDINGS_ACTION_NAME, ::GetFindingsResponse)
}
