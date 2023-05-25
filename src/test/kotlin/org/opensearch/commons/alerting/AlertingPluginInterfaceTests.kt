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
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.Settings
import org.opensearch.commons.alerting.action.AcknowledgeAlertRequest
import org.opensearch.commons.alerting.action.AcknowledgeAlertResponse
import org.opensearch.commons.alerting.action.DeleteMonitorRequest
import org.opensearch.commons.alerting.action.DeleteMonitorResponse
import org.opensearch.commons.alerting.action.DeleteWorkflowRequest
import org.opensearch.commons.alerting.action.DeleteWorkflowResponse
import org.opensearch.commons.alerting.action.GetAlertsRequest
import org.opensearch.commons.alerting.action.GetAlertsResponse
import org.opensearch.commons.alerting.action.GetFindingsRequest
import org.opensearch.commons.alerting.action.GetFindingsResponse
import org.opensearch.commons.alerting.action.GetWorkflowRequest
import org.opensearch.commons.alerting.action.GetWorkflowResponse
import org.opensearch.commons.alerting.action.IndexMonitorRequest
import org.opensearch.commons.alerting.action.IndexMonitorResponse
import org.opensearch.commons.alerting.action.IndexWorkflowRequest
import org.opensearch.commons.alerting.action.IndexWorkflowResponse
import org.opensearch.commons.alerting.action.PublishFindingsRequest
import org.opensearch.commons.alerting.action.SubscribeFindingsResponse
import org.opensearch.commons.alerting.model.FindingDocument
import org.opensearch.commons.alerting.model.FindingWithDocs
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.index.seqno.SequenceNumbers
import org.opensearch.rest.RestStatus
import org.opensearch.search.SearchModule

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
        val namedWriteableRegistry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<IndexMonitorResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.indexMonitor(client, request, namedWriteableRegistry, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun indexWorkflow() {
        val workflow = randomWorkflow()

        val request = mock(IndexWorkflowRequest::class.java)
        val response = IndexWorkflowResponse(
            Workflow.NO_ID,
            Workflow.NO_VERSION,
            SequenceNumbers.UNASSIGNED_SEQ_NO,
            SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
            workflow
        )
        val listener: ActionListener<IndexWorkflowResponse> =
            mock(ActionListener::class.java) as ActionListener<IndexWorkflowResponse>
        val namedWriteableRegistry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<IndexWorkflowResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.indexWorkflow(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun indexBucketMonitor() {
        val monitor = randomBucketLevelMonitor()

        val request = mock(IndexMonitorRequest::class.java)
        val response = IndexMonitorResponse(Monitor.NO_ID, Monitor.NO_VERSION, SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, monitor)
        val listener: ActionListener<IndexMonitorResponse> =
            mock(ActionListener::class.java) as ActionListener<IndexMonitorResponse>
        val namedWriteableRegistry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<IndexMonitorResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())
        AlertingPluginInterface.indexMonitor(client, request, namedWriteableRegistry, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun deleteMonitor() {
        val request = mock(DeleteMonitorRequest::class.java)
        val response = DeleteMonitorResponse(Monitor.NO_ID, Monitor.NO_VERSION)
        val listener: ActionListener<DeleteMonitorResponse> =
            mock(ActionListener::class.java) as ActionListener<DeleteMonitorResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<DeleteMonitorResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.deleteMonitor(client, request, listener)
    }

    @Test
    fun deleteWorkflow() {
        val request = mock(DeleteWorkflowRequest::class.java)
        val response = DeleteWorkflowResponse(Workflow.NO_ID, Workflow.NO_VERSION)
        val listener: ActionListener<DeleteWorkflowResponse> =
            mock(ActionListener::class.java) as ActionListener<DeleteWorkflowResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<DeleteWorkflowResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.deleteWorkflow(client, request, listener)
    }

    @Test
    fun getWorkflow() {
        val request = mock(GetWorkflowRequest::class.java)
        val response = GetWorkflowResponse(
            id = "id", version = 1, seqNo = 1, primaryTerm = 1, status = RestStatus.OK, workflow = randomWorkflow()
        )
        val listener: ActionListener<GetWorkflowResponse> =
            mock(ActionListener::class.java) as ActionListener<GetWorkflowResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<GetWorkflowResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())

        AlertingPluginInterface.getWorkflow(client, request, listener)
    }

    @Test
    fun getAlerts() {
        val monitor = randomQueryLevelMonitor()
        val alert = randomAlert(monitor)
        val request = mock(GetAlertsRequest::class.java)
        val response = GetAlertsResponse(listOf(alert), 1)
        val listener: ActionListener<GetAlertsResponse> =
            mock(ActionListener::class.java) as ActionListener<GetAlertsResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<GetAlertsResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())
        AlertingPluginInterface.getAlerts(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun getFindings() {
        val finding = randomFinding()
        val documentIds = finding.relatedDocIds
        val relatedDocs = mutableListOf<FindingDocument>()
        val request = mock(GetFindingsRequest::class.java)
        val documents: Map<String, FindingDocument> = mutableMapOf()
        for (docId in documentIds) {
            val key = "${finding.index}|$docId"
            documents[key]?.let { document -> relatedDocs.add(document) }
        }
        val findingWithDocs = FindingWithDocs(finding, relatedDocs)
        val response = GetFindingsResponse(RestStatus.OK, 1, listOf(findingWithDocs))
        val listener: ActionListener<GetFindingsResponse> =
            mock(ActionListener::class.java) as ActionListener<GetFindingsResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<GetFindingsResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())
        AlertingPluginInterface.getFindings(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun publishFindings() {
        val request = mock(PublishFindingsRequest::class.java)
        val response = SubscribeFindingsResponse(status = RestStatus.OK)
        val listener: ActionListener<SubscribeFindingsResponse> =
            mock(ActionListener::class.java) as ActionListener<SubscribeFindingsResponse>

        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<SubscribeFindingsResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())
        AlertingPluginInterface.publishFinding(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }

    @Test
    fun acknowledgeAlerts() {
        val request = mock(AcknowledgeAlertRequest::class.java)
        val response = AcknowledgeAlertResponse(acknowledged = listOf(), failed = listOf(), missing = listOf())
        val listener: ActionListener<AcknowledgeAlertResponse> =
            mock(ActionListener::class.java) as ActionListener<AcknowledgeAlertResponse>
        Mockito.doAnswer {
            (it.getArgument(2) as ActionListener<AcknowledgeAlertResponse>)
                .onResponse(response)
        }.whenever(client).execute(Mockito.any(ActionType::class.java), Mockito.any(), Mockito.any())
        AlertingPluginInterface.acknowledgeAlerts(client, request, listener)
        Mockito.verify(listener, Mockito.times(1)).onResponse(ArgumentMatchers.eq(response))
    }
}
