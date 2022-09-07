package org.opensearch.commons.alerting

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.ActionType
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.alerting.action.IndexMonitorRequest
import org.opensearch.commons.alerting.action.IndexMonitorResponse
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.index.seqno.SequenceNumbers

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class AlertingPluginInterfaceTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: NodeClient

    @Test
    fun indexMonitor() {
        val monitor = randomQueryLevelMonitor()

        val request = mock(IndexMonitorRequest::class.java)
        val response = IndexMonitorResponse(Monitor.NO_ID, Monitor.NO_VERSION, SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, monitor)
        val listener: ActionListener<IndexMonitorResponse> =
            mock(ActionListener::class.java) as ActionListener<IndexMonitorResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<IndexMonitorResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.indexMonitor(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }
}
