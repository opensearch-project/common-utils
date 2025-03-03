/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.replication

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.support.clustermanager.AcknowledgedResponse
import org.opensearch.commons.replication.action.StopIndexReplicationRequest
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.transport.client.node.NodeClient

@ExtendWith(MockitoExtension::class)
internal class ReplicationPluginInterfaceTests {

    @Test
    fun `test stopReplication successful response`() {
        // Mock dependencies
        val client: NodeClient = mock()
        val request: StopIndexReplicationRequest = mock()
        val listener: ActionListener<AcknowledgedResponse> = mock()
        val acknowledgedResponse = AcknowledgedResponse(true) // Successful response

        // Mock the behavior of NodeClient.execute()
        whenever(client.execute(any(), any(), any<ActionListener<ActionResponse>>()))
            .thenAnswer { invocation ->
                val actionListener = invocation.getArgument<ActionListener<ActionResponse>>(2)
                actionListener.onResponse(acknowledgedResponse) // Simulate success
            }

        // Call method under test
        ReplicationPluginInterface.stopReplication(client, request, listener)
        // Verify that listener.onResponse is called with the correct response
        verify(listener).onResponse(acknowledgedResponse)
    }

    @Test
    fun `test stopReplication failure response`() {
        // Mock dependencies
        val client: NodeClient = mock()
        val request: StopIndexReplicationRequest = mock()
        val listener: ActionListener<AcknowledgedResponse> = mock()
        val exception = Exception("Test failure")

        // Mock the behavior of NodeClient.execute()
        whenever(client.execute(any(), any(), any<ActionListener<ActionResponse>>()))
            .thenAnswer { invocation ->
                val actionListener = invocation.getArgument<ActionListener<ActionResponse>>(2)
                actionListener.onFailure(exception) // Simulate failure
            }

        // Call method under test
        ReplicationPluginInterface.stopReplication(client, request, listener)
        // Verify that listener.onResponse is called with the correct response
        verify(listener).onFailure(exception)
    }
}
