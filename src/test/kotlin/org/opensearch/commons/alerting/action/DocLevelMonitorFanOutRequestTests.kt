/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.ActionExecutionTime
import org.opensearch.commons.alerting.model.DocLevelMonitorInput
import org.opensearch.commons.alerting.model.DocLevelQuery
import org.opensearch.commons.alerting.model.IndexExecutionContext
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.MonitorMetadata
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.commons.alerting.model.WorkflowRunContext
import org.opensearch.commons.alerting.randomDocumentLevelMonitor
import org.opensearch.commons.alerting.randomDocumentLevelTrigger
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.index.shard.ShardId
import org.opensearch.index.seqno.SequenceNumbers
import org.opensearch.script.Script
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class DocLevelMonitorFanOutRequestTests {

    @Test
    fun `test doc level monitor fan out request as stream`() {
        val docQuery = DocLevelQuery(query = "test_field:\"us-west-2\"", fields = listOf(), name = "3")
        val docLevelInput = DocLevelMonitorInput("description", listOf("test-index"), listOf(docQuery))

        val trigger = randomDocumentLevelTrigger(condition = Script("return true"))
        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger),
            enabled = true,
            schedule = IntervalSchedule(1, ChronoUnit.MINUTES)
        )
        val monitorMetadata = MonitorMetadata(
            "test",
            SequenceNumbers.UNASSIGNED_SEQ_NO,
            SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
            Monitor.NO_ID,
            listOf(ActionExecutionTime("", Instant.now())),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            mutableMapOf("test-index" to ".opensearch-sap-test_windows-queries-000001")
        )
        val indexExecutionContext = IndexExecutionContext(
            listOf(docQuery),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            "test-index",
            "test-index",
            listOf("test-index"),
            listOf("test-index"),
            listOf("test-field"),
            listOf("1", "2")
        )
        val workflowRunContext = WorkflowRunContext(
            Workflow.NO_ID,
            Workflow.NO_ID,
            Monitor.NO_ID,
            mutableMapOf("index" to listOf("1")),
            true
        )
        val docLevelMonitorFanOutRequest = DocLevelMonitorFanOutRequest(
            monitor,
            false,
            monitorMetadata,
            UUID.randomUUID().toString(),
            indexExecutionContext,
            listOf(ShardId("test-index", UUID.randomUUID().toString(), 0)),
            listOf("test-index"),
            workflowRunContext
        )
        val out = BytesStreamOutput()
        docLevelMonitorFanOutRequest.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDocLevelMonitorFanOutRequest = DocLevelMonitorFanOutRequest(sin)
        assertEquals(docLevelMonitorFanOutRequest.monitor, newDocLevelMonitorFanOutRequest.monitor)
        assertEquals(docLevelMonitorFanOutRequest.executionId, newDocLevelMonitorFanOutRequest.executionId)
        assertEquals(docLevelMonitorFanOutRequest.monitorMetadata, newDocLevelMonitorFanOutRequest.monitorMetadata)
        assertEquals(docLevelMonitorFanOutRequest.indexExecutionContext, newDocLevelMonitorFanOutRequest.indexExecutionContext)
        assertEquals(docLevelMonitorFanOutRequest.shardIds, newDocLevelMonitorFanOutRequest.shardIds)
        assertEquals(docLevelMonitorFanOutRequest.workflowRunContext, newDocLevelMonitorFanOutRequest.workflowRunContext)
    }

    @Test
    fun `test doc level monitor fan out request as stream with matching docIds with findings per index`() {
        val docQuery = DocLevelQuery(query = "test_field:\"us-west-2\"", fields = listOf(), name = "3")
        val docLevelInput = DocLevelMonitorInput("description", listOf("test-index"), listOf(docQuery))

        val trigger = randomDocumentLevelTrigger(condition = Script("return true"))
        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger),
            enabled = true,
            schedule = IntervalSchedule(1, ChronoUnit.MINUTES)
        )
        val monitorMetadata = MonitorMetadata(
            "test",
            SequenceNumbers.UNASSIGNED_SEQ_NO,
            SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
            Monitor.NO_ID,
            listOf(ActionExecutionTime("", Instant.now())),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            mutableMapOf("test-index" to ".opensearch-sap-test_windows-queries-000001")
        )
        val indexExecutionContext = IndexExecutionContext(
            listOf(docQuery),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            mutableMapOf("index" to mutableMapOf("1" to "1")),
            "test-index",
            "test-index",
            listOf("test-index"),
            listOf("test-index"),
            listOf("test-field"),
            listOf("1", "2")
        )
        val workflowRunContext = WorkflowRunContext(
            Workflow.NO_ID,
            Workflow.NO_ID,
            Monitor.NO_ID,
            mutableMapOf("index" to listOf("1")),
            true,
            listOf("finding1")
        )
        val docLevelMonitorFanOutRequest = DocLevelMonitorFanOutRequest(
            monitor,
            false,
            monitorMetadata,
            UUID.randomUUID().toString(),
            indexExecutionContext,
            listOf(ShardId("test-index", UUID.randomUUID().toString(), 0)),
            listOf("test-index"),
            workflowRunContext
        )
        val out = BytesStreamOutput()
        docLevelMonitorFanOutRequest.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDocLevelMonitorFanOutRequest = DocLevelMonitorFanOutRequest(sin)
        assertEquals(docLevelMonitorFanOutRequest.monitor, newDocLevelMonitorFanOutRequest.monitor)
        assertEquals(docLevelMonitorFanOutRequest.executionId, newDocLevelMonitorFanOutRequest.executionId)
        assertEquals(docLevelMonitorFanOutRequest.monitorMetadata, newDocLevelMonitorFanOutRequest.monitorMetadata)
        assertEquals(docLevelMonitorFanOutRequest.indexExecutionContext, newDocLevelMonitorFanOutRequest.indexExecutionContext)
        assertEquals(docLevelMonitorFanOutRequest.shardIds, newDocLevelMonitorFanOutRequest.shardIds)
        assertEquals(docLevelMonitorFanOutRequest.workflowRunContext, newDocLevelMonitorFanOutRequest.workflowRunContext)
    }
}
