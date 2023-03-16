/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.rest.RestRequest
import java.io.IOException

class GetWorkflowRequest : ActionRequest {
    val workflowId: String
    val method: RestRequest.Method

    constructor(
        workflowId: String,
        method: RestRequest.Method
    ) : super() {
        this.workflowId = workflowId
        this.method = method
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // workflowId
        sin.readEnum(RestRequest.Method::class.java) // method
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeEnum(method)
    }
}
