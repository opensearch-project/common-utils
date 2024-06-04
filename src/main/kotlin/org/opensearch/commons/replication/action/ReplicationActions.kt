/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.replication.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.master.AcknowledgedResponse

/**
 * All the transport action information for the Replication plugin
 */
object ReplicationActions {

    /**
     * Stop replication. Internal only - Inter plugin communication.
     */
    const val STOP_REPLICATION_NAME = "indices:admin/plugins/replication/index/stop"
    const val STOP_REPLICATION_BASE_ACTION_NAME = "indices:admin/plugins/replication/index/unfollow"

    /**
     * Stop replication transport action type. Internal only - Inter plugin communication.
     */
    val STOP_REPLICATION_ACTION_TYPE =
        ActionType(STOP_REPLICATION_NAME, ::AcknowledgedResponse)

}
