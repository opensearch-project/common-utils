package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.Finding
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException
import java.util.Collections

class PublishFindingsRequest : ActionRequest {

    val monitorId: String

    val findings: List<Finding>

    constructor(
        monitorId: String,
        findings: List<Finding>
    ) : super() {
        this.monitorId = monitorId
        this.findings = findings
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitorId = sin.readString(),
        findings = Collections.unmodifiableList(sin.readList(::Finding))
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        out.writeCollection(findings)
    }
}
