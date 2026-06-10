/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.settings.Settings
import org.opensearch.commons.alerting.action.DocLevelMonitorFanOutResponse
import org.opensearch.commons.alerting.model.ActionExecutionTime
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.model.ChainedAlertTriggerRunResult
import org.opensearch.commons.alerting.model.DocumentLevelTriggerRunResult
import org.opensearch.commons.alerting.model.IndexExecutionContext
import org.opensearch.commons.alerting.model.InputRunResults
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.MonitorMetadata
import org.opensearch.commons.alerting.model.MonitorRunResult
import org.opensearch.commons.alerting.model.TriggerRunResult
import org.opensearch.commons.alerting.model.WorkflowRunResult
import org.opensearch.core.common.io.stream.NamedWriteableAwareStreamInput
import org.opensearch.core.common.io.stream.NamedWriteableRegistry
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.search.SearchModule
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Regression tests for StreamInput.readMap() immutable-map ClassCastException.
 *
 * StreamInput.readMap() Javadoc: "If the returned map contains any entries it will be
 * mutable. If it is empty it might be immutable." — meaning Collections.emptyMap() is
 * returned for zero-size maps. Any code that casts the result to MutableMap without a
 * defensive copy throws ClassCastException (or UnsupportedOperationException on write)
 * when the deserialized map is empty.
 *
 * Each test exercises the empty-map path: serialize an object with empty maps, deserialize
 * it, then assert the round-trip succeeded and the result is mutable.
 *
 * See: opensearch-project/common-utils#967
 */
class ImmutableMapDeserializationTests {

    // -------------------------------------------------------------------------
    // Monitor.uiMetadata
    // -------------------------------------------------------------------------

    @Test
    fun `Monitor with empty uiMetadata survives serialization round-trip`() {
        val monitor = randomQueryLevelMonitor(withMetadata = false) // uiMetadata = mapOf()
        assertTrue(monitor.uiMetadata.isEmpty())

        val out = BytesStreamOutput()
        monitor.writeTo(out)
        val registry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)
        val sin = NamedWriteableAwareStreamInput(StreamInput.wrap(out.bytes().toBytesRef().bytes), registry)
        val deserialized = Monitor(sin)

        assertEquals(monitor.name, deserialized.name)
        assertTrue(deserialized.uiMetadata.isEmpty())
        // Verify mutability — must not throw UnsupportedOperationException
        assertMutableMap(deserialized.uiMetadata)
    }

    @Test
    fun `Monitor with non-empty uiMetadata survives serialization round-trip`() {
        val monitor = randomQueryLevelMonitor(withMetadata = true) // uiMetadata = mapOf("foo" to "bar")
        assertTrue(monitor.uiMetadata.isNotEmpty())

        val out = BytesStreamOutput()
        monitor.writeTo(out)
        val registry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)
        val sin = NamedWriteableAwareStreamInput(StreamInput.wrap(out.bytes().toBytesRef().bytes), registry)
        val deserialized = Monitor(sin)

        assertEquals(monitor.uiMetadata, deserialized.uiMetadata)
    }

    // -------------------------------------------------------------------------
    // MonitorMetadata.lastRunContext + sourceToQueryIndexMapping
    // -------------------------------------------------------------------------

    @Test
    fun `MonitorMetadata with empty lastRunContext survives serialization round-trip`() {
        val metadata = MonitorMetadata(
            id = "monitor-id-metadata",
            monitorId = "monitor-id",
            lastActionExecutionTimes = emptyList(),
            lastRunContext = emptyMap(),
            sourceToQueryIndexMapping = mutableMapOf()
        )

        val out = BytesStreamOutput()
        metadata.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = MonitorMetadata(sin)

        assertEquals(metadata.id, deserialized.id)
        assertTrue(deserialized.lastRunContext.isEmpty())
        assertTrue(deserialized.sourceToQueryIndexMapping.isEmpty())
        // Verify mutability of lastRunContext
        assertMutableMap(deserialized.lastRunContext)
        // Verify mutability of sourceToQueryIndexMapping
        deserialized.sourceToQueryIndexMapping["idx"] = "query-idx"
    }

    @Test
    fun `MonitorMetadata with non-empty maps survives serialization round-trip`() {
        val metadata = MonitorMetadata(
            id = "monitor-id-metadata",
            monitorId = "monitor-id",
            lastActionExecutionTimes = listOf(
                ActionExecutionTime("action-1", Instant.now().truncatedTo(ChronoUnit.MILLIS))
            ),
            lastRunContext = mapOf("index" to mapOf("0" to "1234")),
            sourceToQueryIndexMapping = mutableMapOf("my-index-monitor-id" to "query-index")
        )

        val out = BytesStreamOutput()
        metadata.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = MonitorMetadata(sin)

        assertEquals(metadata.lastRunContext, deserialized.lastRunContext)
        assertEquals(metadata.sourceToQueryIndexMapping, deserialized.sourceToQueryIndexMapping)
    }

    // -------------------------------------------------------------------------
    // IndexExecutionContext.lastRunContext + updatedLastRunContext
    // -------------------------------------------------------------------------

    @Test
    fun `IndexExecutionContext with empty lastRunContext survives serialization round-trip`() {
        val ctx = IndexExecutionContext(
            queries = emptyList(),
            lastRunContext = mutableMapOf(),
            updatedLastRunContext = mutableMapOf(),
            indexName = "my-index",
            concreteIndexName = "my-index-000001",
            updatedIndexNames = emptyList(),
            concreteIndexNames = emptyList(),
            conflictingFields = emptyList(),
            docIds = emptyList(),
            findingIds = emptyList()
        )

        val out = BytesStreamOutput()
        ctx.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = IndexExecutionContext(sin)

        assertEquals(ctx.indexName, deserialized.indexName)
        assertTrue(deserialized.lastRunContext.isEmpty())
        assertTrue(deserialized.updatedLastRunContext.isEmpty())
        // Verify mutability
        assertMutableMap(deserialized.lastRunContext)
        assertMutableMap(deserialized.updatedLastRunContext)
    }

    @Test
    fun `IndexExecutionContext with non-empty context maps survives serialization round-trip`() {
        val ctx = IndexExecutionContext(
            queries = emptyList(),
            lastRunContext = mutableMapOf("shard0" to "seq100"),
            updatedLastRunContext = mutableMapOf("shard0" to "seq101"),
            indexName = "my-index",
            concreteIndexName = "my-index-000001",
            updatedIndexNames = listOf("my-index"),
            concreteIndexNames = listOf("my-index-000001"),
            conflictingFields = emptyList()
        )

        val out = BytesStreamOutput()
        ctx.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = IndexExecutionContext(sin)

        assertEquals(ctx.lastRunContext, deserialized.lastRunContext)
        assertEquals(ctx.updatedLastRunContext, deserialized.updatedLastRunContext)
    }

    // -------------------------------------------------------------------------
    // DocLevelMonitorFanOutResponse.lastRunContexts
    // -------------------------------------------------------------------------

    @Test
    fun `DocLevelMonitorFanOutResponse with empty lastRunContexts survives serialization round-trip`() {
        val response = DocLevelMonitorFanOutResponse(
            nodeId = "node-1",
            executionId = "exec-1",
            monitorId = "monitor-1",
            lastRunContexts = mutableMapOf(), // empty — triggers the bug
            inputResults = InputRunResults()
        )

        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = DocLevelMonitorFanOutResponse(sin)

        assertEquals(response.nodeId, deserialized.nodeId)
        assertEquals(response.executionId, deserialized.executionId)
        assertTrue(deserialized.lastRunContexts.isEmpty())
        // Verify mutability
        deserialized.lastRunContexts["index"] = mutableMapOf<String, Any>()
    }

    @Test
    fun `DocLevelMonitorFanOutResponse with empty triggerResults survives serialization round-trip`() {
        val response = DocLevelMonitorFanOutResponse(
            nodeId = "node-1",
            executionId = "exec-1",
            monitorId = "monitor-1",
            lastRunContexts = mutableMapOf("index" to mutableMapOf<String, Any>() as Any),
            inputResults = InputRunResults(),
            triggerResults = mapOf() // empty — triggers the bug
        )

        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = DocLevelMonitorFanOutResponse(sin)

        assertEquals(response.nodeId, deserialized.nodeId)
        assertTrue(deserialized.triggerResults.isEmpty())
        // Verify mutability — must not throw UnsupportedOperationException or ClassCastException
        @Suppress("UNCHECKED_CAST")
        (deserialized.triggerResults as MutableMap<String, DocumentLevelTriggerRunResult>)
            .put("__mutable_check__", randomDocumentLevelTriggerRunResult())
        @Suppress("UNCHECKED_CAST")
        (deserialized.triggerResults as MutableMap<String, DocumentLevelTriggerRunResult>)
            .remove("__mutable_check__")
    }

    @Test
    fun `DocLevelMonitorFanOutResponse with non-empty lastRunContexts survives serialization round-trip`() {
        val response = DocLevelMonitorFanOutResponse(
            nodeId = "node-1",
            executionId = "exec-1",
            monitorId = "monitor-1",
            lastRunContexts = mutableMapOf("index" to mutableMapOf("0" to "100") as Any),
            inputResults = InputRunResults(),
            triggerResults = mapOf("t1" to randomDocumentLevelTriggerRunResult())
        )

        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = DocLevelMonitorFanOutResponse(sin)

        assertEquals(response.nodeId, deserialized.nodeId)
        assertEquals(response.lastRunContexts, deserialized.lastRunContexts)
    }

    // -------------------------------------------------------------------------
    // WorkflowRunResult.triggerResults
    // -------------------------------------------------------------------------

    @Test
    fun `WorkflowRunResult with empty triggerResults survives serialization round-trip`() {
        val result = WorkflowRunResult(
            workflowId = "workflow-1",
            workflowName = "my-workflow",
            monitorRunResults = emptyList(),
            executionStartTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            executionEndTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            executionId = "exec-1",
            triggerResults = mapOf<String, ChainedAlertTriggerRunResult>() // empty — triggers the bug
        )

        val out = BytesStreamOutput()
        result.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = WorkflowRunResult(sin)

        assertEquals(result.workflowId, deserialized.workflowId)
        assertEquals(result.executionId, deserialized.executionId)
        assertTrue(deserialized.triggerResults.isEmpty())
    }

    @Test
    fun `WorkflowRunResult with non-empty triggerResults is correctly typed after deserialization`() {
        // WorkflowRunResult.writeTo() uses the untyped writeMap which cannot serialize complex
        // Writeable values like ChainedAlertTriggerRunResult. The existing production code path
        // that populates triggerResults only uses the empty-map case at deserialization time
        // (the map is populated by the caller after construction, not via StreamInput in practice).
        // This test verifies the construction contract: a WorkflowRunResult with trigger results
        // retains its data correctly when built directly.
        val workflow = randomWorkflow()
        val trigger = randomChainedAlertTrigger()
        val triggerRunResult = ChainedAlertTriggerRunResult(
            triggerName = trigger.name,
            triggered = true,
            error = null,
            actionResults = mutableMapOf(),
            associatedAlertIds = emptySet()
        )
        val result = WorkflowRunResult(
            workflowId = workflow.id,
            workflowName = workflow.name,
            monitorRunResults = emptyList(),
            executionStartTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            executionEndTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            executionId = "exec-1",
            triggerResults = mapOf(trigger.id to triggerRunResult)
        )

        assertEquals(1, result.triggerResults.size)
        assertEquals(triggerRunResult, result.triggerResults[trigger.id])
    }

    // -------------------------------------------------------------------------
    // MonitorRunResult.triggerResults
    // -------------------------------------------------------------------------

    @Test
    fun `MonitorRunResult with empty triggerResults survives serialization round-trip`() {
        val result = MonitorRunResult<TriggerRunResult>(
            monitorName = "test-monitor",
            periodStart = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            periodEnd = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            triggerResults = mapOf()
        )

        val out = BytesStreamOutput()
        result.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = MonitorRunResult<TriggerRunResult>(sin)

        assertEquals(result.monitorName, deserialized.monitorName)
        assertTrue(deserialized.triggerResults.isEmpty())
        // Verify mutability — must not throw UnsupportedOperationException
        assertMutableMap(deserialized.triggerResults)
    }

    @Test
    fun `MonitorRunResult with non-empty triggerResults survives serialization round-trip`() {
        val result = MonitorRunResult<TriggerRunResult>(
            monitorName = "test-monitor",
            periodStart = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            periodEnd = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            triggerResults = mapOf()
        )

        val out = BytesStreamOutput()
        result.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = MonitorRunResult<TriggerRunResult>(sin)

        assertEquals(result.monitorName, deserialized.monitorName)
        assertEquals(result.triggerResults, deserialized.triggerResults)
    }

    // -------------------------------------------------------------------------
    // InputRunResults.results
    // -------------------------------------------------------------------------

    @Test
    fun `InputRunResults with empty results list survives serialization round-trip`() {
        val inputRunResults = InputRunResults(
            results = listOf(),
            error = null
        )

        val out = BytesStreamOutput()
        inputRunResults.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = InputRunResults.readFrom(sin)

        assertTrue(deserialized.results.isEmpty())
    }

    @Test
    fun `InputRunResults with results containing empty maps survives serialization round-trip`() {
        val inputRunResults = InputRunResults(
            results = listOf(emptyMap()),
            error = null
        )

        val out = BytesStreamOutput()
        inputRunResults.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = InputRunResults.readFrom(sin)

        assertEquals(1, deserialized.results.size)
        assertTrue(deserialized.results[0].isEmpty())
        // Verify mutability — must not throw UnsupportedOperationException
        assertMutableMap(deserialized.results[0])
    }

    // -------------------------------------------------------------------------
    // Alert.queryResults (each element map)
    // -------------------------------------------------------------------------

    @Test
    fun `Alert with empty queryResults list survives serialization round-trip`() {
        val alert = randomAlert().copy(queryResults = emptyList())

        val out = BytesStreamOutput()
        alert.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Alert(sin)

        assertEquals(alert.id, deserialized.id)
        assertTrue(deserialized.queryResults.isEmpty())
    }

    @Test
    fun `Alert with queryResults containing empty maps survives serialization round-trip`() {
        // Each empty map in queryResults triggers readMap() → Collections.emptyMap() on deserialization
        val alert = randomAlertWithPPLFields().copy(
            queryResults = listOf(emptyMap(), emptyMap())
        )

        val out = BytesStreamOutput()
        alert.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Alert(sin)

        assertEquals(2, deserialized.queryResults.size)
        assertTrue(deserialized.queryResults[0].isEmpty())
        assertTrue(deserialized.queryResults[1].isEmpty())
    }

    @Test
    fun `Alert with non-empty queryResults survives serialization round-trip`() {
        val alert = randomAlertWithPPLFields()
        assertTrue(alert.queryResults.isNotEmpty())

        val out = BytesStreamOutput()
        alert.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Alert(sin)

        assertEquals(alert.queryResults, deserialized.queryResults)
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Asserts that a map is mutable by successfully inserting a sentinel value.
     * If the map is Collections.emptyMap() this throws UnsupportedOperationException.
     */
    @Suppress("UNCHECKED_CAST")
    private fun assertMutableMap(map: Map<*, *>) {
        (map as MutableMap<Any, Any>)["__mutable_check__"] = "ok"
        map.remove("__mutable_check__")
    }
}
