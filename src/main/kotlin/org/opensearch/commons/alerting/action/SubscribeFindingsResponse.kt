package org.opensearch.commons.alerting.action

import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class SubscribeFindingsResponse : BaseResponse {
    private var status: RestStatus

    constructor(status: RestStatus) : super() {
        this.status = status
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) {
        this.status = sin.readEnum(RestStatus::class.java)
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeEnum(status)
    }

    override fun toXContent(
        builder: XContentBuilder,
        params: ToXContent.Params,
    ): XContentBuilder {
        builder
            .startObject()
            .field("status", status.status)
        return builder.endObject()
    }

    override fun getStatus(): RestStatus = this.status
}
