package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.notifications.action.BaseResponse

class DeleteMonitorResponse : BaseResponse {
    var id: String
    var version: Long

    constructor(
        id: String,
        version: Long
    ) : super() {
        this.id = id
        this.version = version
    }

    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong() // version
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(IndexUtils._ID, id)
            .field(IndexUtils._VERSION, version)
            .endObject()
    }
}
