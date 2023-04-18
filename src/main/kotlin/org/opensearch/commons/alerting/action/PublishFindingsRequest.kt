package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.alerting.model.Finding
import java.io.IOException

class PublishFindingsRequest : ActionRequest {

    val monitorId: String

    val finding: Finding

    constructor(
        monitorId: String,
        finding: Finding
    ) : super() {
        this.monitorId = monitorId
        this.finding = finding
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitorId = sin.readString(),
        finding = Finding.readFrom(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        finding.writeTo(out)
    }
}
