package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.rest.RestRequest
import java.io.IOException

class IndexWorkflowRequest : ActionRequest {
    val workflowId: String
    val seqNo: Long
    val primaryTerm: Long
    val refreshPolicy: WriteRequest.RefreshPolicy
    val method: RestRequest.Method
    var workflow: Workflow
    val rbacRoles: List<String>?

    constructor(
        workflowId: String,
        seqNo: Long,
        primaryTerm: Long,
        refreshPolicy: WriteRequest.RefreshPolicy,
        method: RestRequest.Method,
        workflow: Workflow,
        rbacRoles: List<String>? = null
    ) : super() {
        this.workflowId = workflowId
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.refreshPolicy = refreshPolicy
        this.method = method
        this.workflow = workflow
        this.rbacRoles = rbacRoles
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        seqNo = sin.readLong(),
        primaryTerm = sin.readLong(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin),
        method = sin.readEnum(RestRequest.Method::class.java),
        workflow = Workflow.readFrom(sin) as Workflow,
        rbacRoles = sin.readOptionalStringList()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        refreshPolicy.writeTo(out)
        out.writeEnum(method)
        workflow.writeTo(out)
        out.writeOptionalStringCollection(rbacRoles)
    }
}
