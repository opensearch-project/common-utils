package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.Table
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException

class GetAlertsRequest : ActionRequest {
    val table: Table
    val severityLevel: String
    val alertState: String
    val monitorId: String?
    val alertIndex: String?
    val monitorIds: List<String>?
    val workflowIds: List<String>?
    val alertIds: List<String>?

    constructor(
        table: Table,
        severityLevel: String,
        alertState: String,
        monitorId: String?,
        alertIndex: String?,
        monitorIds: List<String>? = null,
        workflowIds: List<String>? = null,
        alertIds: List<String>? = null
    ) : super() {
        this.table = table
        this.severityLevel = severityLevel
        this.alertState = alertState
        this.monitorId = monitorId
        this.alertIndex = alertIndex
        this.monitorIds = monitorIds
        this.workflowIds = workflowIds
        this.alertIds = alertIds
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        table = Table.readFrom(sin),
        severityLevel = sin.readString(),
        alertState = sin.readString(),
        monitorId = sin.readOptionalString(),
        alertIndex = sin.readOptionalString(),
        monitorIds = sin.readOptionalStringList(),
        workflowIds = sin.readOptionalStringList(),
        alertIds = sin.readOptionalStringList()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        table.writeTo(out)
        out.writeString(severityLevel)
        out.writeString(alertState)
        out.writeOptionalString(monitorId)
        out.writeOptionalString(alertIndex)
        out.writeOptionalStringCollection(monitorIds)
        out.writeOptionalStringCollection(workflowIds)
        out.writeOptionalStringCollection(alertIds)
    }
}
