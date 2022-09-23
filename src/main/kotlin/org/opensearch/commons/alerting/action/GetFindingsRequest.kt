package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.Table
import java.io.IOException

class GetFindingsRequest : ActionRequest {
    val findingId: String?
    val table: Table
    val monitorId: String?
    val findingIndex: String?

    constructor(
        findingId: String?,
        table: Table,
        monitorId: String? = null,
        findingIndexName: String? = null
    ) : super() {
        this.findingId = findingId
        this.table = table
        this.monitorId = monitorId
        this.findingIndex = findingIndexName
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        findingId = sin.readOptionalString(),
        table = Table.readFrom(sin),
        monitorId = sin.readOptionalString(),
        findingIndexName = sin.readOptionalString()
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
    }
}
