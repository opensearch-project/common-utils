package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import java.io.IOException

class DeleteWorkflowRequest : ActionRequest {

    val workflowId: String
    /**
     * Flag that indicates whether the delegate monitors should be deleted or not.
     * If the flag is set to true, Delegate monitors will be deleted only in the case when they are part of the specified workflow and no other.
     */
    val deleteDelegateMonitors: Boolean?

    constructor(workflowId: String, deleteDelegateMonitors: Boolean?) : super() {
        this.workflowId = workflowId
        this.deleteDelegateMonitors = deleteDelegateMonitors
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        deleteDelegateMonitors = sin.readOptionalBoolean()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeOptionalBoolean(deleteDelegateMonitors)
    }
}
