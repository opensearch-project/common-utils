/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.apache.logging.log4j.LogManager
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.DataSources
import org.opensearch.commons.alerting.model.IndexExecutionContext
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.Monitor.Companion.NO_VERSION
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
        // flag flipped to true whenever a safeRead*() method fails to serialize a field correctly
        private var serializationFailedFlag: Boolean = false
        val log = LogManager.getLogger(DocLevelMonitorFanOutRequest::class.java)
        private fun safeReadMonitor(sin: StreamInput): Monitor =
            try {
                Monitor.readFrom(sin)!!
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing monitor in Doc level monitor fanout request", e)
                Monitor(
                    "failed_serde", NO_VERSION, "failed_serde", true,
                    IntervalSchedule(1, ChronoUnit.MINUTES), Instant.now(), Instant.now(), "",
                    null, NO_SCHEMA_VERSION, emptyList(), emptyList(), emptyMap(),
                    DataSources(), false, false, "failed"
                )
            }

        private fun safeReadBoolean(sin: StreamInput): Boolean =
            try {
                sin.readBoolean()
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing boolean in Doc level monitor fanout request", e)
                false
            }

        private fun safeReadMonitorMetadata(sin: StreamInput): MonitorMetadata =
            try {
                MonitorMetadata.readFrom(sin)
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing monitor in Doc level monitor fanout request", e)
                MonitorMetadata(
                    "failed_serde",
                    SequenceNumbers.UNASSIGNED_SEQ_NO,
                    SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
                    "failed_serde",
                    emptyList(),
                    emptyMap(),
                    mutableMapOf()
                )
            }

        private fun safeReadString(sin: StreamInput): String =
            try {
                sin.readString()
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing string in Doc level monitor fanout request", e)
                ""
            }

        private fun safeReadShardIds(sin: StreamInput): List<ShardId> =
            try {
                sin.readList(::ShardId)
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing shardId list in Doc level monitor fanout request", e)
                listOf(ShardId("failed_serde", "failed_serde", 999999))
            }

        private fun safeReadStringList(sin: StreamInput): List<String> =
            try {
                sin.readStringList()
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing string list in Doc level monitor fanout request", e)
                emptyList()
            }

        private fun safeReadWorkflowRunContext(sin: StreamInput): WorkflowRunContext? =
            try {
                if (sin.readBoolean()) WorkflowRunContext(sin) else null
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing workflow context in Doc level monitor fanout request", e)
                null
            }

        private fun safeReadIndexExecutionContext(sin: StreamInput): IndexExecutionContext? {
            var indexExecutionContext: IndexExecutionContext? = null
            return try {
                indexExecutionContext = IndexExecutionContext(sin)
                while (sin.read() != -1) {
                    // read and discard bytes until stream is entirely consumed
                    try {
                        sin.readByte()
                    } catch (_: EOFException) {
                    }
                }
                return indexExecutionContext
            } catch (e: EOFException) {
                indexExecutionContext
            } catch (e: Exception) {
                serializationFailedFlag = true
                log.error("Error parsing index execution context in Doc level monitor fanout request", e)
                while (sin.read() != -1) {
                    try { // read and throw bytes until stream is entirely consumed
                        sin.readByte()
                    } catch (_: EOFException) {
                    }
                }
                null
            }
        }
    }

    constructor(
        monitor: Monitor,
        dryRun: Boolean,
        monitorMetadata: MonitorMetadata,
        executionId: String,
        indexExecutionContext: IndexExecutionContext?,
        shardIds: List<ShardId>,
        concreteIndicesSeenSoFar: List<String>,
        workflowRunContext: WorkflowRunContext?,
        hasSerializationFailed: Boolean? = null
    ) : super() {
        this.monitor = monitor
        this.dryRun = dryRun
        this.monitorMetadata = monitorMetadata
        this.executionId = executionId
        this.indexExecutionContext = indexExecutionContext
        this.shardIds = shardIds
        this.concreteIndicesSeenSoFar = concreteIndicesSeenSoFar
        this.workflowRunContext = workflowRunContext
        require(false == shardIds.isEmpty()) { }
        this.hasSerializationFailed = hasSerializationFailed ?: false
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitor = safeReadMonitor(sin),
        dryRun = safeReadBoolean(sin),
        monitorMetadata = safeReadMonitorMetadata(sin),
        executionId = safeReadString(sin),
        shardIds = safeReadShardIds(sin),
        concreteIndicesSeenSoFar = safeReadStringList(sin),
        workflowRunContext = safeReadWorkflowRunContext(sin),
        indexExecutionContext = safeReadIndexExecutionContext(sin),
        hasSerializationFailed = serializationFailedFlag
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
