package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.rest.RestRequest
import java.io.IOException

class ToggleMonitorRequest : ActionRequest {
    val monitorId: String
    val enabled: Boolean
    val seqNo: Long
    val primaryTerm: Long
    val method: RestRequest.Method

    constructor(
        monitorId: String,
        enabled: Boolean,
        seqNo: Long,
        primaryTerm: Long,
        method: RestRequest.Method,
    ) : super() {
        this.monitorId = monitorId
        this.enabled = enabled
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.method = method
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this (
        monitorId = sin.readString(),
        enabled = sin.readBoolean(),
        seqNo = sin.readLong(),
        primaryTerm = sin.readLong(),
        method = sin.readEnum(RestRequest.Method::class.java),
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        out.writeBoolean(enabled)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        out.writeEnum(method)
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
