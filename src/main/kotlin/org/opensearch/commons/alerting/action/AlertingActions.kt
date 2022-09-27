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

    @JvmField
    val INDEX_MONITOR_ACTION_TYPE =
        ActionType(INDEX_MONITOR_ACTION_NAME, ::IndexMonitorResponse)
    @JvmField
    val GET_ALERTS_ACTION_TYPE =
        ActionType(GET_ALERTS_ACTION_NAME, ::GetAlertsResponse)
    @JvmField
    val DELETE_MONITOR_ACTION_TYPE =
        ActionType(DELETE_MONITOR_ACTION_NAME, ::DeleteMonitorResponse)
    @JvmField
    val GET_FINDINGS_ACTION_TYPE =
        ActionType(GET_FINDINGS_ACTION_NAME, ::GetFindingsResponse)
}
