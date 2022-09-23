/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.common.io.stream.Writeable
import org.opensearch.commons.alerting.action.AlertingActions
import org.opensearch.commons.alerting.action.DeleteMonitorRequest
import org.opensearch.commons.alerting.action.DeleteMonitorResponse
import org.opensearch.commons.alerting.action.GetAlertsRequest
import org.opensearch.commons.alerting.action.GetAlertsResponse
import org.opensearch.commons.alerting.action.GetFindingsRequest
import org.opensearch.commons.alerting.action.GetFindingsResponse
import org.opensearch.commons.alerting.action.IndexMonitorRequest
import org.opensearch.commons.alerting.action.IndexMonitorResponse
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.utils.recreateObject

/**
 * All the transport action plugin interfaces for the Alerting plugin
 */
object AlertingPluginInterface {

    /**
     * Index monitor interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun indexMonitor(
        client: NodeClient,
        request: IndexMonitorRequest,
        listener: ActionListener<IndexMonitorResponse>
    ) {
        client.execute(
            AlertingActions.INDEX_MONITOR_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    IndexMonitorResponse(
                        it
                    )
                }
            }
        )
    }

    fun deleteMonitor(
        client: NodeClient,
        request: DeleteMonitorRequest,
        listener: ActionListener<DeleteMonitorResponse>
    ) {
        client.execute(
            AlertingActions.DELETE_MONITOR_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    DeleteMonitorResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Get Alerts interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getAlerts(
        client: NodeClient,
        request: GetAlertsRequest,
        listener: ActionListener<GetAlertsResponse>
    ) {
        client.execute(
            AlertingActions.GET_ALERTS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    GetAlertsResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Get Findings interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getFindings(
        client: NodeClient,
        request: GetFindingsRequest,
        listener: ActionListener<GetFindingsResponse>
    ) {
        client.execute(
            AlertingActions.GET_FINDINGS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    GetFindingsResponse(
                        it
                    )
                }
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Response : BaseResponse> wrapActionListener(
        listener: ActionListener<Response>,
        recreate: (Writeable) -> Response
    ): ActionListener<Response> {
        return object : ActionListener<ActionResponse> {
            override fun onResponse(response: ActionResponse) {
                val recreated = response as? Response ?: recreate(response)
                listener.onResponse(recreated)
            }

            override fun onFailure(exception: java.lang.Exception) {
                listener.onFailure(exception)
            }
        } as ActionListener<Response>
    }
}
