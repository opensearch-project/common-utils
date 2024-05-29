package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.rest.RestRequest
import java.io.IOException

class IndexNoteRequest : ActionRequest {
    val entityId: String
    val noteId: String
    val seqNo: Long
    val primaryTerm: Long
    val method: RestRequest.Method
    var content: String

    constructor(
        entityId: String,
        noteId: String,
        seqNo: Long,
        primaryTerm: Long,
        method: RestRequest.Method,
        content: String
    ) : super() {
        this.entityId = entityId
        this.noteId = noteId
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.method = method
        this.content = content
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        entityId = sin.readString(),
        noteId = sin.readString(),
        seqNo = sin.readLong(),
        primaryTerm = sin.readLong(),
        method = sin.readEnum(RestRequest.Method::class.java),
        content = sin.readString()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(entityId)
        out.writeString(noteId)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        out.writeEnum(method)
        out.writeString(content)
    }
}