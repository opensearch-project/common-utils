package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.DocRequest
import org.opensearch.commons.alerting.util.AlertingConstants.Companion.ALL_COMMENTS_INDEX_PATTERN
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import java.io.IOException

class DeleteCommentRequest : ActionRequest, DocRequest {
    val commentId: String

    constructor(commentId: String) : super() {
        this.commentId = commentId
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        commentId = sin.readString()
    )

    override fun validate(): ActionRequestValidationException? {
        if (commentId.isBlank()) {
            val exception = ActionRequestValidationException()
            exception.addValidationError("comment id must not be blank")
            return exception
        }
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(commentId)
    }

    override fun index(): String? {
        return ALL_COMMENTS_INDEX_PATTERN
    }

    override fun id(): String? {
        return commentId
    }
}
