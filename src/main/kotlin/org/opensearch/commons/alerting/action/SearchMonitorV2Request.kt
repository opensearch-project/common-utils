package org.opensearch.commons.alerting.action

import java.io.IOException
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.search.SearchRequest
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput

class SearchMonitorV2Request : ActionRequest {
    val searchRequest: SearchRequest

    constructor(
        searchRequest: SearchRequest
    ) : super() {
        this.searchRequest = searchRequest
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        searchRequest = SearchRequest(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        searchRequest.writeTo(out)
    }
}