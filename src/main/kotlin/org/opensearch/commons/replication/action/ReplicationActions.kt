/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.replication.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.clustermanager.AcknowledgedResponse

/**
 * Information related to the transport stop replication action for the Replication plugin
 */
object ReplicationActions {
    /**
     * Action names for stopping replication
     * STOP_REPLICATION_ACTION_NAME: action used for _replication/_stop REST API
     * INTERNAL_STOP_REPLICATION_ACTION_NAME: Internal only - Used by Index Management plugin to invoke stop replication
     */
    const val STOP_REPLICATION_ACTION_NAME = "indices:admin/plugins/replication/index/stop"
    const val INTERNAL_STOP_REPLICATION_ACTION_NAME = "indices:internal/plugins/replication/index/stop"

    /**
     * Stop replication transport action types.
     */
    val STOP_REPLICATION_ACTION_TYPE =
        ActionType(STOP_REPLICATION_ACTION_NAME, ::AcknowledgedResponse)
    val INTERNAL_STOP_REPLICATION_ACTION_TYPE =
        ActionType(INTERNAL_STOP_REPLICATION_ACTION_NAME, ::AcknowledgedResponse)
}
