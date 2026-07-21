package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.DocRequest
import org.opensearch.action.support.WriteRequest
import org.opensearch.commons.alerting.model.ScheduledJob
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException

class DeleteMonitorRequest : DocRequest, ActionRequest {

    val monitorId: String
    val refreshPolicy: WriteRequest.RefreshPolicy

    constructor(monitorId: String, refreshPolicy: WriteRequest.RefreshPolicy) : super() {
        this.monitorId = monitorId
        this.refreshPolicy = refreshPolicy
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitorId = sin.readString(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        refreshPolicy.writeTo(out)
    }

    override fun index(): String? {
        return ScheduledJob.SCHEDULED_JOBS_INDEX
    }

    override fun id(): String? {
        return monitorId
    }
}
