/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.replication.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.master.AcknowledgedResponse

/**
 * Information related to the transport stop replication action for the Replication plugin
 */
object ReplicationActions {

    /**
     * Action names for stopping replication
     * STOP_REPLICATION_ACTION_NAME: action used for _stop REST API
     * UNFOLLOW_REPLICATION_ACTION_NAME: internal action used for inter-plugin communication i.e. by ism to invoke stop
     * replication.
     */
    const val STOP_REPLICATION_ACTION_NAME = "indices:admin/plugins/replication/index/stop"
    const val UNFOLLOW_REPLICATION_ACTION_NAME = "indices:admin/plugins/replication/index/unfollow"

    /**
     * Stop replication transport action types.
     */
    val STOP_REPLICATION_ACTION_TYPE =
        ActionType(STOP_REPLICATION_ACTION_NAME, ::AcknowledgedResponse)
    val UNFOLLOW_REPLICATION_ACTION_TYPE =
        ActionType(UNFOLLOW_REPLICATION_ACTION_NAME, ::AcknowledgedResponse)
}
