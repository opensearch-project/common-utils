package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import java.io.IOException

class DeleteMonitorRequest : ActionRequest {

    val monitorId: String
    val refreshPolicy: WriteRequest.RefreshPolicy
    val workflowMetadataId: String?

    constructor(monitorId: String, refreshPolicy: WriteRequest.RefreshPolicy, workflowMetadataId: String? = null) : super() {
        this.monitorId = monitorId
        this.refreshPolicy = refreshPolicy
        this.workflowMetadataId = workflowMetadataId
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitorId = sin.readString(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin),
        workflowMetadataId = sin.readOptionalString()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        refreshPolicy.writeTo(out)
        out.writeOptionalString(workflowMetadataId)
    }
}
