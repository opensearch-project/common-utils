/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

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
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class DocLevelMonitorFanOutRequest : ActionRequest, ToXContentObject {
    val monitor: Monitor
    val dryRun: Boolean
    val monitorMetadata: MonitorMetadata
    val executionId: String
    val indexExecutionContext: IndexExecutionContext?
    val shardIds: List<ShardId>
    val concreteIndicesSeenSoFar: List<String>
    val workflowRunContext: WorkflowRunContext?

    constructor(
        monitor: Monitor,
        dryRun: Boolean,
        monitorMetadata: MonitorMetadata,
        executionId: String,
        indexExecutionContext: IndexExecutionContext?,
        shardIds: List<ShardId>,
        concreteIndicesSeenSoFar: List<String>,
        workflowRunContext: WorkflowRunContext?,
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
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitor = try {
            Monitor.readFrom(sin)!!
        } catch (e: Exception) {
            Monitor(
                "failed_serde",
                NO_VERSION,
                "failed_serde",
                true,
                IntervalSchedule(1, ChronoUnit.MINUTES),
                Instant.now(),
                Instant.now(),
                "",
                null,
                NO_SCHEMA_VERSION,
                emptyList(),
                emptyList(),
                emptyMap(),
                DataSources(),
                false,
                false,
                "failed"
            )
        },
        dryRun = try {
            sin.readBoolean()
        } catch (e: Exception) {
            false
        },
        monitorMetadata = try {
            MonitorMetadata.readFrom(sin)
        } catch (e: Exception) {
            MonitorMetadata(
                "failed_serde", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
                "failed_serde", emptyList(), emptyMap(),
                mutableMapOf()
            )
        },
        executionId = try {
            sin.readString()
        } catch (e: Exception) {
            ""
        },
        shardIds = try {
            sin.readList(::ShardId)
        } catch (e: Exception) {
            listOf(ShardId("failed_serde", "failed_serde",999999))// to circumvent an isEmpty() check in constructor
        },
        concreteIndicesSeenSoFar = try {
            sin.readStringList()
        } catch (e: Exception) {
            emptyList()
        },
        workflowRunContext = try {
            if (sin.readBoolean()) {
                WorkflowRunContext(sin)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        },
        indexExecutionContext = try {
            IndexExecutionContext(sin)
        } catch (e: Exception) {
            IndexExecutionContext(
                emptyList(), mutableMapOf(), mutableMapOf(), "failed_serde", "failed_serde", emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList()
            )
        }
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
        builder.startObject()
            .field("monitor", monitor)
            .field("dry_run", dryRun)
            .field("execution_id", executionId)
            .field("index_execution_context", indexExecutionContext)
            .field("shard_ids", shardIds)
            .field("concrete_indices", concreteIndicesSeenSoFar)
            .field("workflow_run_context", workflowRunContext)
        return builder.endObject()
    }
}
