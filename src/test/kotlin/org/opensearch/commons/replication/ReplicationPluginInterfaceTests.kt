/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.replication

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionType
import org.opensearch.action.support.master.AcknowledgedResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.replication.action.StopIndexReplicationRequest
import org.opensearch.core.action.ActionListener

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class ReplicationPluginInterfaceTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: NodeClient
    @Test
    fun stopReplication() {
        val request = Mockito.mock(StopIndexReplicationRequest::class.java)
        val response = AcknowledgedResponse(true)
        val listener: ActionListener<AcknowledgedResponse> =
            Mockito.mock(ActionListener::class.java) as ActionListener<AcknowledgedResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<AcknowledgedResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        ReplicationPluginInterface.stopReplication(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }
}