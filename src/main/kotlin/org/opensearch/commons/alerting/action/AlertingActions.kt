/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionType
import org.opensearch.action.search.SearchResponse

object AlertingActions {
    const val INDEX_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/write"
    const val INDEX_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/write"
    const val GET_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/get"
    const val GET_WORKFLOW_ALERTS_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow_alerts/get"
    const val GET_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/get"
    const val DELETE_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/delete"
    const val DELETE_WORKFLOW_ACTION_NAME = "cluster:admin/opensearch/alerting/workflow/delete"
    const val GET_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/get"
    const val ACKNOWLEDGE_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/ack"
    const val ACKNOWLEDGE_CHAINED_ALERTS_ACTION_NAME = "cluster:admin/opendistro/alerting/chained_alerts/ack"
    const val SUBSCRIBE_FINDINGS_ACTION_NAME = "cluster:admin/opensearch/alerting/findings/subscribe"
    const val GET_MONITOR_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/get"
    const val SEARCH_MONITORS_ACTION_NAME = "cluster:admin/opendistro/alerting/monitor/search"
    const val INDEX_COMMENT_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/comments/write"
    const val SEARCH_COMMENTS_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/comments/search"
    const val DELETE_COMMENT_ACTION_NAME = "cluster:admin/opendistro/alerting/alerts/comments/delete"

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
    val GET_WORKFLOW_ALERTS_ACTION_TYPE =
        ActionType(GET_WORKFLOW_ALERTS_ACTION_NAME, ::GetWorkflowAlertsResponse)

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

    @JvmField
    val ACKNOWLEDGE_CHAINED_ALERTS_ACTION_TYPE =
        ActionType(ACKNOWLEDGE_CHAINED_ALERTS_ACTION_NAME, ::AcknowledgeAlertResponse)

    @JvmField
    val GET_MONITOR_ACTION_TYPE =
        ActionType(GET_MONITOR_ACTION_NAME, ::GetMonitorResponse)

    @JvmField
    val SEARCH_MONITORS_ACTION_TYPE =
        ActionType(SEARCH_MONITORS_ACTION_NAME, ::SearchResponse)

    @JvmField
    val INDEX_COMMENT_ACTION_TYPE =
        ActionType(INDEX_COMMENT_ACTION_NAME, ::IndexCommentResponse)

    @JvmField
    val SEARCH_COMMENTS_ACTION_TYPE =
        ActionType(SEARCH_COMMENTS_ACTION_NAME, ::SearchResponse)

    @JvmField
    val DELETE_COMMENT_ACTION_TYPE =
        ActionType(DELETE_COMMENT_ACTION_NAME, ::DeleteCommentResponse)
}
