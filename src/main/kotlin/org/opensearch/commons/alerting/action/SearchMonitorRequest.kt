/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.Version
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.search.SearchRequest
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException

class SearchMonitorRequest : ActionRequest {

    val searchRequest: SearchRequest

    // When true, the caller has opted in to seeing the backend roles it is entitled to on each monitor hit.
    // Off by default, so search responses are unchanged unless the caller asks.
    val includeBackendRoles: Boolean

    constructor(
        searchRequest: SearchRequest,
        includeBackendRoles: Boolean = false
    ) : super() {
        this.searchRequest = searchRequest
        this.includeBackendRoles = includeBackendRoles
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        searchRequest = SearchRequest(sin),
        includeBackendRoles = if (sin.version.onOrAfter(Version.V_3_10_0)) sin.readBoolean() else false
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        searchRequest.writeTo(out)
        if (out.version.onOrAfter(Version.V_3_10_0)) out.writeBoolean(includeBackendRoles)
    }
}
