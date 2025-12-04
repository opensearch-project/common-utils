/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.Version
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder

data class WorkflowRunContext(
    // In case of dry run it's random generated id, while in other cases it's workflowId
    val workflowId: String,
    val workflowMetadataId: String,
    val chainedMonitorId: String?,
    val matchingDocIdsPerIndex: Map<String, List<String>>,
    val auditDelegateMonitorAlerts: Boolean,
    val findingIds: List<String>? = null,
) : Writeable,
    ToXContentObject {
    companion object {
        fun readFrom(sin: StreamInput): WorkflowRunContext = WorkflowRunContext(sin)
    }

    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        workflowMetadataId = sin.readString(),
        chainedMonitorId = sin.readOptionalString(),
        matchingDocIdsPerIndex =
            sin.readMap(
                StreamInput::readString,
                StreamInput::readStringList,
            ),
        auditDelegateMonitorAlerts = sin.readBoolean(),
        findingIds = if (sin.version.onOrAfter(Version.V_2_15_0)) sin.readOptionalStringList() else emptyList(),
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeString(workflowMetadataId)
        out.writeOptionalString(chainedMonitorId)
        out.writeMap(matchingDocIdsPerIndex)
        out.writeBoolean(auditDelegateMonitorAlerts)
        if (out.version.onOrAfter(Version.V_2_15_0)) {
            out.writeOptionalStringCollection(findingIds)
        }
    }

    override fun toXContent(
        builder: XContentBuilder,
        params: ToXContent.Params?,
    ): XContentBuilder {
        builder
            .startObject()
            .field("workflow_id", workflowId)
            .field("workflow_metadata_id", workflowMetadataId)
            .field("chained_monitor_id", chainedMonitorId)
            .field("matching_doc_ids_per_index", matchingDocIdsPerIndex)
            .field("audit_delegate_monitor_alerts", auditDelegateMonitorAlerts)
            .field("finding_ids", findingIds)
            .endObject()
        return builder
    }
}
