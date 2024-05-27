package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException

class DeleteNoteRequest : ActionRequest {
    val noteId: String

    constructor(noteId: String) : super() {
        this.noteId = noteId
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        noteId = sin.readString()
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(noteId)
    }
}
