package org.opensearch.commons.sql.action

import org.opensearch.action.ActionType

object SQLActions {

    /**
     * Send SQL query. Internal only - Inter plugin communication.
     */
    const val SEND_SQL_QUERY_NAME = "cluster:admin/opensearch/sql"

    /**
     * Send PPL query. Internal only - Inter plugin communication.
     */
    const val SEND_PPL_QUERY_NAME = "cluster:admin/opensearch/ppl"

    /**
     * Send SQL query transport action type.
     */
    val SEND_SQL_QUERY_ACTION_TYPE =
        ActionType(SEND_SQL_QUERY_NAME, ::TransportSQLQueryResponse)

    /**
     * Send PPL query transport action type.
     */
    val SEND_PPL_QUERY_ACTION_TYPE =
        ActionType(SEND_PPL_QUERY_NAME, ::TransportPPLQueryResponse)
}
