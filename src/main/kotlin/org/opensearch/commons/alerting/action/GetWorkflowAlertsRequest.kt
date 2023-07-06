package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.Table
import java.io.IOException

class GetWorkflowAlertsRequest : ActionRequest {
    val table: Table
    val severityLevel: String
    val alertState: String
    val alertIndex: String?
    val monitorIds: List<String>?
    val workflowIds: List<String>?
    val alertIds: List<String>?
    val getAssociatedAlerts: Boolean

    constructor(
        table: Table,
        severityLevel: String,
        alertState: String,
        alertIndex: String?,
        monitorIds: List<String>? = null,
        workflowIds: List<String>? = null,
        alertIds: List<String>? = null,
        getAssociatedAlerts: Boolean
    ) : super() {
        this.table = table
        this.severityLevel = severityLevel
        this.alertState = alertState
        this.alertIndex = alertIndex
        this.monitorIds = monitorIds
        this.workflowIds = workflowIds
        this.alertIds = alertIds
        this.getAssociatedAlerts = getAssociatedAlerts
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        table = Table.readFrom(sin),
        severityLevel = sin.readString(),
        alertState = sin.readString(),
        alertIndex = sin.readOptionalString(),
        monitorIds = sin.readOptionalStringList(),
        workflowIds = sin.readOptionalStringList(),
        alertIds = sin.readOptionalStringList(),
        getAssociatedAlerts = sin.readBoolean()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        table.writeTo(out)
        out.writeString(severityLevel)
        out.writeString(alertState)
        out.writeOptionalString(alertIndex)
        out.writeOptionalStringCollection(monitorIds)
        out.writeOptionalStringCollection(workflowIds)
        out.writeOptionalStringCollection(alertIds)
        out.writeBoolean(getAssociatedAlerts)
    }
}
