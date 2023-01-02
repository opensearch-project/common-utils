package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import java.io.IOException

class DeleteWorkflowRequest : ActionRequest {

    val workflowId: String
    val refreshPolicy: WriteRequest.RefreshPolicy

    constructor(workflowId: String, refreshPolicy: WriteRequest.RefreshPolicy) : super() {
        this.workflowId = workflowId
        this.refreshPolicy = refreshPolicy
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        refreshPolicy.writeTo(out)
    }
}
