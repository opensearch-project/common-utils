package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

class DeleteCommentResponse : BaseResponse {
    var commentId: String

    constructor(
        id: String
    ) : super() {
        this.commentId = id
    }

    constructor(sin: StreamInput) : this(
        sin.readString() // commentId
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(commentId)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(IndexUtils._ID, commentId)
            .endObject()
    }
}
