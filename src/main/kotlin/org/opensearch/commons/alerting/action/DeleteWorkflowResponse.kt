package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

class DeleteWorkflowResponse : BaseResponse {
    var id: String
    var version: Long
    var nonDeletedMonitors: List<String>? = null

    constructor(
        id: String,
        version: Long,
        nonDeletedMonitors: List<String>? = null
    ) : super() {
        this.id = id
        this.version = version
        this.nonDeletedMonitors = nonDeletedMonitors
    }

    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong(), // version
        sin.readOptionalStringList()
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeOptionalStringCollection(nonDeletedMonitors)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(IndexUtils._ID, id)
            .field(IndexUtils._VERSION, version)
            .field(NON_DELETED_MONITORS, nonDeletedMonitors)
            .endObject()
    }

    companion object {
        const val NON_DELETED_MONITORS = "NON_DELETED_MONITORS"
    }
}
