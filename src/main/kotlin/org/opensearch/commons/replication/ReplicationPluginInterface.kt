/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.replication

import org.opensearch.action.support.master.AcknowledgedResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.replication.action.StopIndexReplicationRequest
import org.opensearch.commons.replication.action.ReplicationActions.UNFOLLOW_REPLICATION_ACTION_TYPE
import org.opensearch.commons.utils.recreateObject
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.Writeable


/**
 * Transport action plugin interfaces for the cross-cluster-replication plugin.
 */
object ReplicationPluginInterface {

    /**
     * Stop replication.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */

    fun stopReplication(
        client: NodeClient,
        request: StopIndexReplicationRequest,
        listener: ActionListener<AcknowledgedResponse>
    ) {
        return client.execute(
            UNFOLLOW_REPLICATION_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response ->
                recreateObject(response) {
                    AcknowledgedResponse(it)
                }
            }
        )
    }

    /**
     * Wrap action listener on concrete response class by a new created one on ActionResponse.
     * This is required because the response may be loaded by different classloader across plugins.
     * The onResponse(ActionResponse) avoids type cast exception and give a chance to recreate
     * the response object.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <Response : AcknowledgedResponse> wrapActionListener(
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
