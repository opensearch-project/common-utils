package org.opensearch.commons.alerting.action

import java.io.IOException
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.ppl.action.TransportPPLQueryRequest
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.search.fetch.subphase.FetchSourceContext

class PingPPLSQLRequest : ActionRequest {
    val queryRequest: TransportPPLQueryRequest

    constructor(
        queryRequest: TransportPPLQueryRequest
    ) : super() {
        this.queryRequest = queryRequest
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        TransportPPLQueryRequest(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        queryRequest.writeTo(out)
    }
}
