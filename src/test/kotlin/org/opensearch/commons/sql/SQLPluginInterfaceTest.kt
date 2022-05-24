/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.ActionType
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.sql.action.TransportPPLQueryRequest
import org.opensearch.commons.sql.action.TransportPPLQueryResponse
import org.opensearch.commons.sql.action.TransportSQLQueryResponse

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class SQLPluginInterfaceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: NodeClient

    @Test
    fun sendSQLQuery() {
        val query = "SELECT * FROM account;"
        val response = TransportSQLQueryResponse("sample response")
        val listener: ActionListener<TransportSQLQueryResponse> =
            Mockito.mock(ActionListener::class.java) as ActionListener<TransportSQLQueryResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<TransportSQLQueryResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        SQLPluginInterface.sendSQLQuery(client, query, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun sendPPLQuery() {
        val query = "search source=accounts"
        val path = "plugin/_ppl"
        val format = ""
        val response = TransportPPLQueryResponse("sample response")
        val listener: ActionListener<TransportPPLQueryResponse> =
            Mockito.mock(ActionListener::class.java) as ActionListener<TransportPPLQueryResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<TransportPPLQueryResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        SQLPluginInterface.sendPPLQuery(client, TransportPPLQueryRequest(query, path, format, threadContext = null), listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }
}
