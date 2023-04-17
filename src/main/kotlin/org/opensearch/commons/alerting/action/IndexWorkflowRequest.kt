package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.CompositeInput
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.rest.RestRequest
import java.io.IOException
import java.util.stream.Collectors

class IndexWorkflowRequest : ActionRequest {
    val workflowId: String
    val seqNo: Long
    val primaryTerm: Long
    val refreshPolicy: WriteRequest.RefreshPolicy
    val method: RestRequest.Method
    var workflow: Workflow
    val rbacRoles: List<String>?

    private val MAX_DELEGATE_SIZE = 25

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
        var validationException: ActionRequestValidationException? = null

        if (workflow.inputs.isEmpty()) {
            validationException = ValidateActions.addValidationError(
                "Input list can not be empty.", validationException
            )
            return validationException
        }
        if (workflow.inputs.size > 1) {
            validationException = ValidateActions.addValidationError(
                "Input list can contain only one element.", validationException
            )
            return validationException
        }
        if (workflow.inputs[0] !is CompositeInput) {
            validationException = ValidateActions.addValidationError(
                "When creating a workflow input must be CompositeInput", validationException
            )
        }
        val compositeInput = workflow.inputs[0] as CompositeInput
        val monitorIds = compositeInput.sequence.delegates.stream().map { it.monitorId }.collect(Collectors.toList())

        if (monitorIds.isNullOrEmpty()) {
            validationException = ValidateActions.addValidationError(
                "Delegates list can not be empty.", validationException
            )
            // Break the flow because next checks are dependant on non-null monitorIds
            return validationException
        }

        if (monitorIds.size > MAX_DELEGATE_SIZE) {
            validationException = ValidateActions.addValidationError(
                "Delegates list can not be larger then $MAX_DELEGATE_SIZE.", validationException
            )
        }

        if (monitorIds.toSet().size != monitorIds.size) {
            validationException = ValidateActions.addValidationError(
                "Duplicate delegates not allowed", validationException
            )
        }
        val delegates = compositeInput.sequence.delegates
        val orderSet = delegates.stream().filter { it.order > 0 }.map { it.order }.collect(Collectors.toSet())
        if (orderSet.size != delegates.size) {
            validationException = ValidateActions.addValidationError(
                "Sequence ordering of delegate monitor shouldn't contain duplicate order values", validationException
            )
        }

        val monitorIdOrderMap: Map<String, Int> = delegates.associate { it.monitorId to it.order }
        delegates.forEach {
            if (it.chainedMonitorFindings != null) {
                if (monitorIdOrderMap.containsKey(it.chainedMonitorFindings!!.monitorId) == false) {
                    validationException = ValidateActions.addValidationError(
                        "Chained Findings Monitor ${it.chainedMonitorFindings!!.monitorId} doesn't exist in sequence",
                        validationException
                    )
                    // Break the flow because next check will generate the NPE
                    return validationException
                }
                if (it.order <= monitorIdOrderMap[it.chainedMonitorFindings!!.monitorId]!!) {
                    validationException = ValidateActions.addValidationError(
                        "Chained Findings Monitor ${it.chainedMonitorFindings!!.monitorId} should be executed before monitor ${it.monitorId}",
                        validationException
                    )
                }
            }
        }
        return validationException
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
