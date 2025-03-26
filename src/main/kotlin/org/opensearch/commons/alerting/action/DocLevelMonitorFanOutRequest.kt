/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.apache.logging.log4j.LogManager
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.Alert.Companion.NO_VERSION
import org.opensearch.commons.alerting.model.DataSources
import org.opensearch.commons.alerting.model.IndexExecutionContext
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.MonitorMetadata
import org.opensearch.commons.alerting.model.WorkflowRunContext
import org.opensearch.commons.alerting.util.IndexUtils.Companion.NO_SCHEMA_VERSION
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.index.shard.ShardId
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.index.seqno.SequenceNumbers
import java.io.EOFException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

class DocLevelMonitorFanOutRequest : ActionRequest, ToXContentObject {

    val monitor: Monitor
    val dryRun: Boolean
    val monitorMetadata: MonitorMetadata
    val executionId: String
    val indexExecutionContext: IndexExecutionContext?
    val shardIds: List<ShardId>
    val concreteIndicesSeenSoFar: List<String>
    val workflowRunContext: WorkflowRunContext?
    val hasSerializationFailed: Boolean

    companion object {
        val log = LogManager.getLogger(DocLevelMonitorFanOutRequest::class.java)
    }

    constructor(
        monitor: Monitor,
        dryRun: Boolean,
        monitorMetadata: MonitorMetadata,
        executionId: String,
        indexExecutionContext: IndexExecutionContext?,
        shardIds: List<ShardId>,
        concreteIndicesSeenSoFar: List<String>,
        workflowRunContext: WorkflowRunContext?
    ) : super() {
        this.monitor = monitor
        this.dryRun = dryRun
        this.monitorMetadata = monitorMetadata
        this.executionId = executionId
        this.indexExecutionContext = indexExecutionContext
        this.shardIds = shardIds
        this.concreteIndicesSeenSoFar = concreteIndicesSeenSoFar
        this.workflowRunContext = workflowRunContext
        this.hasSerializationFailed = false
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        var monitorSerializationSucceeded = true
        var parsedMonitor = getDummyMonitor()
        var parsedDryRun = false
        var parsedMonitorMetadata: MonitorMetadata = MonitorMetadata(
            "failed_serde",
            SequenceNumbers.UNASSIGNED_SEQ_NO,
            SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
            "failed_serde",
            emptyList(),
            emptyMap(),
            mutableMapOf()
        )
        var parsedShardIds: List<ShardId> = emptyList()
        var parsedConcreteIndicesSeenSoFar = mutableListOf<String>()
        var parsedExecutionId: String = ""
        var parsedWorkflowContext: WorkflowRunContext? = null
        var parsedIndexExecutionContext: IndexExecutionContext? = null
        try {
            parsedMonitor = Monitor(sin)
            parsedDryRun = sin.readBoolean()
            parsedMonitorMetadata = MonitorMetadata.readFrom(sin)
            parsedExecutionId = sin.readString()
            parsedShardIds = sin.readList(::ShardId)
            parsedConcreteIndicesSeenSoFar = sin.readStringList()
            parsedWorkflowContext = if (sin.readBoolean()) {
                WorkflowRunContext(sin)
            } else {
                null
            }
            parsedIndexExecutionContext = IndexExecutionContext(sin)
        } catch (e: Exception) {
            log.error("Error parsing monitor in Doc level monitor fanout request", e)
            monitorSerializationSucceeded = false
            log.info("Force consuming stream in Doc level monitor fanout request")
            while (sin.read() != 0) {
                // read and discard bytes until stream is entirely consumed
                try {
                    sin.readByte()
                } catch (_: EOFException) {
                }
            }
        }

        this.monitor = parsedMonitor
        this.dryRun = parsedDryRun
        this.monitorMetadata = parsedMonitorMetadata
        this.executionId = parsedExecutionId
        this.shardIds = parsedShardIds
        this.concreteIndicesSeenSoFar = parsedConcreteIndicesSeenSoFar
        this.workflowRunContext = parsedWorkflowContext
        this.indexExecutionContext = parsedIndexExecutionContext
        this.hasSerializationFailed = false == monitorSerializationSucceeded
    }

    private fun getDummyMonitor() = Monitor(
        "failed_serde", NO_VERSION, "failed_serde", true,
        IntervalSchedule(1, ChronoUnit.MINUTES), Instant.now(), Instant.now(), "",
        null, NO_SCHEMA_VERSION, emptyList(), emptyList(), emptyMap(),
        DataSources(), false, false, "failed"
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        monitor.writeTo(out)
        out.writeBoolean(dryRun)
        monitorMetadata.writeTo(out)
        out.writeString(executionId)
        out.writeCollection(shardIds)
        out.writeStringCollection(concreteIndicesSeenSoFar)
        out.writeBoolean(workflowRunContext != null)
        workflowRunContext?.writeTo(out)
        indexExecutionContext?.writeTo(out)
    }

    override fun validate(): ActionRequestValidationException? {
        var actionValidationException: ActionRequestValidationException? = null
        if (shardIds.isEmpty()) {
            actionValidationException = ActionRequestValidationException()
            actionValidationException.addValidationError("shard_ids is null or empty")
        }
        return actionValidationException
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field("monitor", monitor)
            .field("dry_run", dryRun)
            .field("execution_id", executionId)
            .field("index_execution_context", indexExecutionContext)
            .field("shard_ids", shardIds)
            .field("concrete_indices", concreteIndicesSeenSoFar)
            .field("workflow_run_context", workflowRunContext)
            .endObject()
    }
}
