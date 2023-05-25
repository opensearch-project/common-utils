/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionType

object AlertingActions {
    const val INDEX_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/write"
    const val INDEX_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/write"
    const val GET_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/get"
    const val GET_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/get"
    const val DELETE_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/delete"
    const val DELETE_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/delete"
    const val GET_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/get"
    const val ACKNOWLEDGE_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/ack"
    const val SUBSCRIBE_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/subscribe"

    @JvmField
    val INDEX_MONITOR_ACTION_TYPE =
        ActionType(INDEX_MONITOR_ACTION_NAME, ::IndexMonitorResponse)
    @JvmField
    val INDEX_WORKFLOW_ACTION_TYPE =
        ActionType(INDEX_WORKFLOW_ACTION_NAME, ::IndexWorkflowResponse)
    @JvmField
    val GET_ALERTS_ACTION_TYPE =
        ActionType(GET_ALERTS_ACTION_NAME, ::GetAlertsResponse)
    @JvmField
    val GET_WORKFLOW_ACTION_TYPE =
        ActionType(GET_WORKFLOW_ACTION_NAME, ::GetWorkflowResponse)

    @JvmField
    val DELETE_MONITOR_ACTION_TYPE =
        ActionType(DELETE_MONITOR_ACTION_NAME, ::DeleteMonitorResponse)
    @JvmField
    val DELETE_WORKFLOW_ACTION_TYPE =
        ActionType(DELETE_WORKFLOW_ACTION_NAME, ::DeleteWorkflowResponse)
    @JvmField
    val GET_FINDINGS_ACTION_TYPE =
        ActionType(GET_FINDINGS_ACTION_NAME, ::GetFindingsResponse)
    @JvmField
    val ACKNOWLEDGE_ALERTS_ACTION_TYPE =
        ActionType(ACKNOWLEDGE_ALERTS_ACTION_NAME, ::AcknowledgeAlertResponse)
    @JvmField
    val SUBSCRIBE_FINDINGS_ACTION_TYPE =
        ActionType(SUBSCRIBE_FINDINGS_ACTION_NAME, ::SubscribeFindingsResponse)
}
