/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._PRIMARY_TERM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._SEQ_NO
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class GetWorkflowResponse : BaseResponse {
    var id: String
    var version: Long
    var seqNo: Long
    var primaryTerm: Long
    private var status: RestStatus
    var workflow: Workflow?

    constructor(
        id: String,
        version: Long,
        seqNo: Long,
        primaryTerm: Long,
        status: RestStatus,
        workflow: Workflow?
    ) : super() {
        this.id = id
        this.version = version
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.status = status
        this.workflow = workflow
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong(), // version
        sin.readLong(), // seqNo
        sin.readLong(), // primaryTerm
        sin.readEnum(RestStatus::class.java), // RestStatus
        if (sin.readBoolean()) {
            Workflow.readFrom(sin) // monitor
        } else {
            null
        }
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        out.writeEnum(status)
        if (workflow != null) {
            out.writeBoolean(true)
            workflow?.writeTo(out)
        } else {
            out.writeBoolean(false)
        }
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(_ID, id)
            .field(_VERSION, version)
            .field(_SEQ_NO, seqNo)
            .field(_PRIMARY_TERM, primaryTerm)
        if (workflow != null) {
            builder.field("workflow", workflow)
        }

        return builder.endObject()
    }

    override fun getStatus(): RestStatus {
        return this.status
    }
}
