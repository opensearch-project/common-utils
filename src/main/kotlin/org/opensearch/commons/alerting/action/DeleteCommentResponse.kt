package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

class DeleteCommentResponse : BaseResponse {
    var id: String

    constructor(
        id: String
    ) : super() {
        this.id = id
    }

    constructor(sin: StreamInput) : this(
        sin.readString() // id
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(IndexUtils._ID, id)
            .endObject()
    }
}
