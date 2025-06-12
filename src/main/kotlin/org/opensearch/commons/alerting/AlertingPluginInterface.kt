/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting

import org.opensearch.action.search.SearchResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.alerting.action.AcknowledgeAlertRequest
import org.opensearch.commons.alerting.action.AcknowledgeAlertResponse
import org.opensearch.commons.alerting.action.AcknowledgeChainedAlertRequest
import org.opensearch.commons.alerting.action.AlertingActions
import org.opensearch.commons.alerting.action.DeleteMonitorRequest
import org.opensearch.commons.alerting.action.DeleteMonitorResponse
import org.opensearch.commons.alerting.action.DeleteWorkflowRequest
import org.opensearch.commons.alerting.action.DeleteWorkflowResponse
import org.opensearch.commons.alerting.action.GetAlertsRequest
import org.opensearch.commons.alerting.action.GetAlertsResponse
import org.opensearch.commons.alerting.action.GetFindingsRequest
import org.opensearch.commons.alerting.action.GetFindingsResponse
import org.opensearch.commons.alerting.action.GetMonitorRequest
import org.opensearch.commons.alerting.action.GetMonitorResponse
import org.opensearch.commons.alerting.action.GetWorkflowAlertsRequest
import org.opensearch.commons.alerting.action.GetWorkflowAlertsResponse
import org.opensearch.commons.alerting.action.GetWorkflowRequest
import org.opensearch.commons.alerting.action.GetWorkflowResponse
import org.opensearch.commons.alerting.action.IndexMonitorRequest
import org.opensearch.commons.alerting.action.IndexMonitorResponse
import org.opensearch.commons.alerting.action.IndexWorkflowRequest
import org.opensearch.commons.alerting.action.IndexWorkflowResponse
import org.opensearch.commons.alerting.action.PublishBatchFindingsRequest
import org.opensearch.commons.alerting.action.PublishFindingsRequest
import org.opensearch.commons.alerting.action.SearchMonitorRequest
import org.opensearch.commons.alerting.action.SubscribeFindingsResponse
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.utils.recreateObject
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.NamedWriteableRegistry
import org.opensearch.core.common.io.stream.Writeable

/**
 * All the transport action plugin interfaces for the Alerting plugin
 */
object AlertingPluginInterface {

    /**
     * Index monitor interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param namedWriteableRegistry Registry for building aggregations
     * @param listener The listener for getting response
     */
    fun indexMonitor(
        client: NodeClient,
        request: IndexMonitorRequest,
        namedWriteableRegistry: NamedWriteableRegistry,
        listener: ActionListener<IndexMonitorResponse>
    ) {
        client.execute(
            AlertingActions.INDEX_MONITOR_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response, namedWriteableRegistry) {
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
     * Index monitor interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param namedWriteableRegistry Registry for building aggregations
     * @param listener The listener for getting response
     */
    fun indexWorkflow(
        client: NodeClient,
        request: IndexWorkflowRequest,
        listener: ActionListener<IndexWorkflowResponse>
    ) {
        client.execute(
            AlertingActions.INDEX_WORKFLOW_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    IndexWorkflowResponse(
                        it
                    )
                }
            }
        )
    }

    fun deleteWorkflow(
        client: NodeClient,
        request: DeleteWorkflowRequest,
        listener: ActionListener<DeleteWorkflowResponse>
    ) {
        client.execute(
            AlertingActions.DELETE_WORKFLOW_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    DeleteWorkflowResponse(
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
     * Get Workflow Alerts interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getWorkflowAlerts(
        client: NodeClient,
        request: GetWorkflowAlertsRequest,
        listener: ActionListener<GetWorkflowAlertsResponse>
    ) {
        client.execute(
            AlertingActions.GET_WORKFLOW_ALERTS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    GetWorkflowAlertsResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Get Workflow interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getWorkflow(
        client: NodeClient,
        request: GetWorkflowRequest,
        listener: ActionListener<GetWorkflowResponse>
    ) {
        client.execute(
            AlertingActions.GET_WORKFLOW_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    GetWorkflowResponse(
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

    /**
     * Acknowledge Alerts interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun acknowledgeAlerts(
        client: NodeClient,
        request: AcknowledgeAlertRequest,
        listener: ActionListener<AcknowledgeAlertResponse>
    ) {
        client.execute(
            AlertingActions.ACKNOWLEDGE_ALERTS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    AcknowledgeAlertResponse(
                        it
                    )
                }
            }
        )
    }

    fun publishFinding(
        client: NodeClient,
        request: PublishFindingsRequest,
        listener: ActionListener<SubscribeFindingsResponse>
    ) {
        client.execute(
            AlertingActions.SUBSCRIBE_FINDINGS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    SubscribeFindingsResponse(
                        it
                    )
                }
            }
        )
    }

    fun publishBatchFindings(
        client: NodeClient,
        request: PublishBatchFindingsRequest,
        listener: ActionListener<SubscribeFindingsResponse>
    ) {
        client.execute(
            AlertingActions.SUBSCRIBE_BATCH_FINDINGS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    SubscribeFindingsResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Acknowledge Chained Alerts interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun acknowledgeChainedAlerts(
        client: NodeClient,
        request: AcknowledgeChainedAlertRequest,
        listener: ActionListener<AcknowledgeAlertResponse>
    ) {
        client.execute(
            AlertingActions.ACKNOWLEDGE_CHAINED_ALERTS_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    AcknowledgeAlertResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Get Monitor interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getMonitor(
        client: NodeClient,
        request: GetMonitorRequest,
        listener: ActionListener<GetMonitorResponse>
    ) {
        client.execute(
            AlertingActions.GET_MONITOR_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    GetMonitorResponse(
                        it
                    )
                }
            }
        )
    }

    /**
     * Search Monitors interface.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun searchMonitors(
        client: NodeClient,
        request: SearchMonitorRequest,
        listener: ActionListener<SearchResponse>
    ) {
        client.execute(
            AlertingActions.SEARCH_MONITORS_ACTION_TYPE,
            request,
            // we do not use the wrapActionListener in this case since there is no need
            // to recreate any object or specially handle onResponse / onFailure. It is
            // simply returning a SearchResponse.
            listener
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
