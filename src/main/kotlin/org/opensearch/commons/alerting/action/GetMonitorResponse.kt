/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._PRIMARY_TERM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._SEQ_NO
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
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

    constructor(
        id: String,
        version: Long,
        seqNo: Long,
        primaryTerm: Long,
        monitor: Monitor?,
        associatedCompositeMonitors: List<AssociatedWorkflow>?,
    ) : super() {
        this.id = id
        this.version = version
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.monitor = monitor
        this.associatedWorkflows = associatedCompositeMonitors ?: emptyList()
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(), // id
        version = sin.readLong(), // version
        seqNo = sin.readLong(), // seqNo
        primaryTerm = sin.readLong(), // primaryTerm
        monitor = if (sin.readBoolean()) {
            Monitor.readFrom(sin) // monitor
        } else null,
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
        out.writeList((associatedWorkflows?: emptyList()) as MutableList<AssociatedWorkflow>?)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(_ID, id)
            .field(_VERSION, version)
            .field(_SEQ_NO, seqNo)
            .field(_PRIMARY_TERM, primaryTerm)
        if (monitor != null) {
            builder.field("monitor", monitor)
        }
        if (associatedWorkflows != null) {
            builder.field("associated_workflows", associatedWorkflows!!.toTypedArray())
        }
        return builder.endObject()
    }

    class AssociatedWorkflow : ToXContentFragment, Writeable {
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

        override fun writeTo(out: StreamOutput) {
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
