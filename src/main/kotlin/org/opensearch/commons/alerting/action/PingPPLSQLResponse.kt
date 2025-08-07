package org.opensearch.commons.alerting.action

import java.io.IOException
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import kotlin.jvm.Throws

class PingPPLSQLResponse : BaseResponse {
    var queryResponse: String?

    @Throws(IOException::class)
    constructor(queryResponse: String?) : super() {
        this.queryResponse = queryResponse
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        this.queryResponse = sin.readOptionalString()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeOptionalString(queryResponse)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field("query_response", queryResponse)
            .endObject()
        return builder
    }
}