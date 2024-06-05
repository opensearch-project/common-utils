package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.model.Comment
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._PRIMARY_TERM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._SEQ_NO
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class IndexCommentResponse : BaseResponse {
    var id: String
    var seqNo: Long
    var primaryTerm: Long
    var comment: Comment

    constructor(
        id: String,
        seqNo: Long,
        primaryTerm: Long,
        comment: Comment
    ) : super() {
        this.id = id
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.comment = comment
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong(), // seqNo
        sin.readLong(), // primaryTerm
        Comment.readFrom(sin) // comment
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        comment.writeTo(out)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(_ID, id)
            .field(_SEQ_NO, seqNo)
            .field(_PRIMARY_TERM, primaryTerm)
            .field("comment", comment)
            .endObject()
    }
}
