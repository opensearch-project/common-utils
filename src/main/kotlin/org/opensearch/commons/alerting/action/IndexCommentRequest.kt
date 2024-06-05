package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.rest.RestRequest
import java.io.IOException

/**
 * Request to index/create a Comment
 *
 * entityId: the entity that the Comment is attached to and therefore associated with (e.g. in Alerting,
 * the entity is an Alert). This field is expected to be non-blank if the request is to create a new Comment.
 *
 * commentId: the ID of an existing Comment. This field is expected to be non-blank if the request is to
 * update an existing Comment.
 */
class IndexCommentRequest : ActionRequest {
    val entityId: String
    val commentId: String
    val seqNo: Long
    val primaryTerm: Long
    val method: RestRequest.Method
    var content: String

    constructor(
        entityId: String,
        commentId: String,
        seqNo: Long,
        primaryTerm: Long,
        method: RestRequest.Method,
        content: String
    ) : super() {
        this.entityId = entityId
        this.commentId = commentId
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.method = method
        this.content = content
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        entityId = sin.readString(),
        commentId = sin.readString(),
        seqNo = sin.readLong(),
        primaryTerm = sin.readLong(),
        method = sin.readEnum(RestRequest.Method::class.java),
        content = sin.readString()
    )

    override fun validate(): ActionRequestValidationException? {
        if (method == RestRequest.Method.POST && entityId.isBlank() ||
            method == RestRequest.Method.PUT && commentId.isBlank()
        ) {
            return ActionRequestValidationException()
        }
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(entityId)
        out.writeString(commentId)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        out.writeEnum(method)
        out.writeString(content)
    }
}
