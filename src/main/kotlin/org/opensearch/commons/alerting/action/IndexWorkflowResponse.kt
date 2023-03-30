package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class IndexWorkflowResponse : BaseResponse {
    var id: String
    var version: Long
    var seqNo: Long
    var primaryTerm: Long
    var workflow: Workflow

    constructor(
        id: String,
        version: Long,
        seqNo: Long,
        primaryTerm: Long,
        workflow: Workflow
    ) : super() {
        this.id = id
        this.version = version
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.workflow = workflow
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong(), // version
        sin.readLong(), // seqNo
        sin.readLong(), // primaryTerm
        Workflow.readFrom(sin) as Workflow // workflow
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        workflow.writeTo(out)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(IndexUtils._ID, id)
            .field(IndexUtils._VERSION, version)
            .field(IndexUtils._SEQ_NO, seqNo)
            .field(IndexUtils._PRIMARY_TERM, primaryTerm)
            .field("workflow", workflow)
            .endObject()
    }
}
