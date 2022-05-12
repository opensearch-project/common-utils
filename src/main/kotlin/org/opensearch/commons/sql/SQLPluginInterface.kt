/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.common.io.stream.Writeable
import org.opensearch.commons.ConfigConstants
import org.opensearch.commons.sql.action.BaseResponse
import org.opensearch.commons.sql.action.SQLActions
import org.opensearch.commons.sql.action.TransportPPLQueryRequest
import org.opensearch.commons.sql.action.TransportPPLQueryResponse
import org.opensearch.commons.sql.action.TransportSQLQueryRequest
import org.opensearch.commons.sql.action.TransportSQLQueryResponse
import org.opensearch.commons.utils.SecureClientWrapper
import org.opensearch.commons.utils.recreateObject

/**
 * All the transport action plugin interfaces for the SQL plugin
 */
object SQLPluginInterface {

    /**
     * Send SQL API enabled for a feature. No REST API. Internal API only for Inter plugin communication.
     * @param client Node client for making transport action
     * @param query The query string
     * @param listener The listener for getting response
     */
    fun sendSQLQuery(
        client: NodeClient,
        query: String,
        listener: ActionListener<TransportSQLQueryResponse>
    ) {
        val threadContext: String? =
            client.threadPool().threadContext.getTransient<String>(ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val wrapper = SecureClientWrapper(client) // Executing request in privileged mode
        wrapper.execute(
            SQLActions.SEND_SQL_QUERY_ACTION_TYPE,
            TransportSQLQueryRequest(query, threadContext),
            wrapActionListener(listener) { response -> recreateObject(response) { TransportSQLQueryResponse(it) } }
        )
    }

    /**
     * Send PPL API enabled for a feature. No REST API. Internal API only for Inter plugin communication.
     * @param client Node client for making transport action
     * @param query The query string
     * @param listener The listener for getting response
     */
    fun sendPPLQuery(
        client: NodeClient,
        query: String,
        listener: ActionListener<TransportPPLQueryResponse>
    ) {
        val threadContext: String? =
            client.threadPool().threadContext.getTransient<String>(ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val wrapper = SecureClientWrapper(client) // Executing request in privileged mode
        wrapper.execute(
            SQLActions.SEND_PPL_QUERY_ACTION_TYPE,
            TransportPPLQueryRequest(query, threadContext),
            wrapActionListener(listener) { response -> recreateObject(response) { TransportPPLQueryResponse(it) } }
        )
    }

    /**
     * Wrap action listener on concrete response class by a new created one on ActionResponse.
     * This is required because the response may be loaded by different classloader across plugins.
     * The onResponse(ActionResponse) avoids type cast exception and give a chance to recreate
     * the response object.
     */
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
