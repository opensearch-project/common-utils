package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.commons.alerting.model.FindingWithDocs
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.rest.RestStatus
import java.io.IOException

class GetFindingsResponse : BaseResponse {
    var totalFindings: Int?
    var findings: List<FindingWithDocs>

    constructor(
        status: RestStatus,
        totalFindings: Int?,
        findings: List<FindingWithDocs>
    ) : super() {
        this.totalFindings = totalFindings
        this.findings = findings
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        status = RestStatus.OK,
        totalFindings = sin.readOptionalInt(),
        findings = sin.readList((FindingWithDocs)::readFrom)
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeOptionalInt(totalFindings)
        out.writeCollection(findings)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field("total_findings", totalFindings)
            .field("findings", findings.toTypedArray())

        return builder.endObject()
    }
}
