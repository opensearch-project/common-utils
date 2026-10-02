/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.Version
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.util.IndexUtils.Companion.INCLUDE_BACKEND_ROLES_PARAM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._PRIMARY_TERM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._SEQ_NO
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentFragment
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class GetMonitorResponse : BaseResponse {
    var id: String
    var version: Long
    var seqNo: Long
    var primaryTerm: Long
    var monitor: Monitor?
    var associatedWorkflows: List<AssociatedWorkflow>?

    /**
     * The monitor's backend roles that the requesting user is entitled to see, resolved by the transport action.
     * When null, no backend roles are written to the response.
     */
    var visibleBackendRoles: List<String>?

    constructor(
        id: String,
        version: Long,
        seqNo: Long,
        primaryTerm: Long,
        monitor: Monitor?,
        associatedCompositeMonitors: List<AssociatedWorkflow>?
    ) : this(id, version, seqNo, primaryTerm, monitor, associatedCompositeMonitors, null)

    constructor(
        id: String,
        version: Long,
        seqNo: Long,
        primaryTerm: Long,
        monitor: Monitor?,
        associatedCompositeMonitors: List<AssociatedWorkflow>?,
        visibleBackendRoles: List<String>?
    ) : super() {
        this.id = id
        this.version = version
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.monitor = monitor
        this.associatedWorkflows = associatedCompositeMonitors ?: emptyList()
        this.visibleBackendRoles = visibleBackendRoles
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(), // id
        version = sin.readLong(), // version
        seqNo = sin.readLong(), // seqNo
        primaryTerm = sin.readLong(), // primaryTerm
        monitor = if (sin.readBoolean()) {
            Monitor.readFrom(sin) // monitor
        } else {
            null
        },
        // Read before associatedWorkflows to match the order writeTo uses.
        visibleBackendRoles = if (sin.version.onOrAfter(Version.V_3_10_0)) {
            sin.readOptionalStringList()
        } else {
            null
        },
        associatedCompositeMonitors = sin.readList((AssociatedWorkflow)::readFrom)
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        if (monitor != null) {
            out.writeBoolean(true)
            monitor?.writeTo(out)
        } else {
            out.writeBoolean(false)
        }
        if (out.version.onOrAfter(Version.V_3_10_0)) {
            out.writeOptionalStringCollection(visibleBackendRoles)
            // associatedWorkflows has always been written element by element, with no count, while the
            // constructor above reads it with readList, which expects one: any stream carrying this response
            // was unreadable. Write the count on 3.10.0+ streams, leaving older ones exactly as they were.
            out.writeVInt(associatedWorkflows?.size ?: 0)
        }
        associatedWorkflows?.forEach {
            it.writeTo(out)
        }
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(_ID, id)
            .field(_VERSION, version)
            .field(_SEQ_NO, seqNo)
            .field(_PRIMARY_TERM, primaryTerm)
        val monitor = this.monitor
        if (monitor != null) {
            builder.field("monitor")
            // Nothing to show means nothing is written: no empty user block for a resource that carries no
            // roles, or whose roles the requester shares none of.
            val visibleBackendRoles = this.visibleBackendRoles
                ?.takeIf { it.isNotEmpty() && params.paramAsBoolean(INCLUDE_BACKEND_ROLES_PARAM, false) }
            if (visibleBackendRoles != null) {
                monitor.toXContentWithBackendRoles(builder, params, visibleBackendRoles)
            } else {
                monitor.toXContent(builder, params)
            }
        }
        if (associatedWorkflows != null) {
            builder.field("associated_workflows", associatedWorkflows!!.toTypedArray())
        }
        return builder.endObject()
    }

    class AssociatedWorkflow : ToXContentFragment {
        val id: String
        val name: String

        constructor(id: String, name: String) {
            this.id = id
            this.name = name
        }

        override fun toXContent(builder: XContentBuilder, params: ToXContent.Params?): XContentBuilder {
            builder.startObject()
                .field("id", id)
                .field("name", name)
                .endObject()
            return builder
        }

        fun writeTo(out: StreamOutput) {
            out.writeString(id)
            out.writeString(name)
        }

        @Throws(IOException::class)
        constructor(sin: StreamInput) : this(
            sin.readString(),
            sin.readString()
        )

        companion object {
            @JvmStatic
            @Throws(IOException::class)
            fun readFrom(sin: StreamInput): AssociatedWorkflow {
                return AssociatedWorkflow(sin)
            }
        }
    }
}
