package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.Table
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException
import java.time.Instant

class GetFindingsRequest : ActionRequest {
    val findingId: String?
    val table: Table
    val monitorId: String?
    val monitorIds: List<String>?
    val findingIndex: String?
    val severity: String?
    val detectionType: String?
    val findingIds: List<String>?
    val startTime: Instant?
    val endTime: Instant?

    constructor(
        findingId: String?,
        table: Table,
        monitorId: String? = null,
        findingIndexName: String? = null,
        monitorIds: List<String>? = null,
        severity: String? = null,
        detectionType: String? = null ,
        findingIds: List<String>? = null,
        startTime: Instant? = null,
        endTime: Instant? = null
    ) : super() {
        this.findingId = findingId
        this.table = table
        this.monitorId = monitorId
        this.findingIndex = findingIndexName
        this.monitorIds = monitorIds
        this.severity = severity
        this.detectionType = detectionType
        this.findingIds = findingIds
        this.startTime = startTime
        this.endTime = endTime
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        findingId = sin.readOptionalString(),
        table = Table.readFrom(sin),
        monitorId = sin.readOptionalString(),
        findingIndexName = sin.readOptionalString(),
        monitorIds = sin.readOptionalStringList(),
        severity = sin.readOptionalString(),
        detectionType = sin.readOptionalString(),
        findingIds = sin.readOptionalStringList(),
        startTime = sin.readOptionalInstant(),
        endTime = sin.readOptionalInstant()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeOptionalString(findingId)
        table.writeTo(out)
        out.writeOptionalString(monitorId)
        out.writeOptionalString(findingIndex)
        out.writeOptionalStringCollection(monitorIds)
        out.writeOptionalString(severity)
        out.writeOptionalString(detectionType)
        out.writeOptionalStringCollection(findingIds)
        out.writeOptionalInstant(startTime)
        out.writeOptionalInstant(endTime)
    }
}
