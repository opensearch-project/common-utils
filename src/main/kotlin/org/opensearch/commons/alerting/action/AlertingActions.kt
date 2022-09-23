/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionType

object AlertingActions {
    const val INDEX_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/write"
    const val GET_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/get"
    const val DELETE_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/delete"
    const val GET_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/get"

    val INDEX_MONITOR_ACTION_TYPE =
        ActionType(INDEX_MONITOR_ACTION_NAME, ::IndexMonitorResponse)

    val GET_ALERTS_ACTION_TYPE =
        ActionType(GET_ALERTS_ACTION_NAME, ::GetAlertsResponse)

    val DELETE_MONITOR_ACTION_TYPE =
        ActionType(DELETE_MONITOR_ACTION_NAME, ::DeleteMonitorResponse)

    val GET_FINDINGS_ACTION_TYPE =
        ActionType(GET_FINDINGS_ACTION_NAME, ::GetFindingsResponse)
}
