package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import java.io.IOException

class DeleteWorkflowRequest : ActionRequest {

    val workflowId: String
    val deleteDelegateMonitors: Boolean?
    val refreshPolicy: WriteRequest.RefreshPolicy

    constructor(workflowId: String, deleteDelegateMonitors: Boolean?, refreshPolicy: WriteRequest.RefreshPolicy) : super() {
        this.workflowId = workflowId
        this.deleteDelegateMonitors = deleteDelegateMonitors
        this.refreshPolicy = refreshPolicy
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        deleteDelegateMonitors = sin.readOptionalBoolean(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeOptionalBoolean(deleteDelegateMonitors)
        refreshPolicy.writeTo(out)
    }
}
