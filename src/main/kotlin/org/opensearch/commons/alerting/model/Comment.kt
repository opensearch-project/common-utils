package org.opensearch.commons.alerting.model

import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.commons.alerting.util.optionalUserField
import org.opensearch.commons.alerting.util.optionalUsernameField
import org.opensearch.commons.authuser.User
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken
import java.io.IOException
import java.time.Instant

data class Comment(
    val id: String = NO_ID,
    val entityId: String = NO_ID,
    val content: String,
    val createdTime: Instant,
    val lastUpdatedTime: Instant?,
    val user: User?
) : Writeable, ToXContent {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        entityId = sin.readString(),
        content = sin.readString(),
        createdTime = sin.readInstant(),
        lastUpdatedTime = sin.readOptionalInstant(),
        user = if (sin.readBoolean()) User(sin) else null
    )

    constructor(
        entityId: String,
        content: String,
        createdTime: Instant,
        user: User?
    ) : this (
        entityId = entityId,
        content = content,
        createdTime = createdTime,
        lastUpdatedTime = null,
        user = user
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(entityId)
        out.writeString(content)
        out.writeInstant(createdTime)
        out.writeOptionalInstant(lastUpdatedTime)
        out.writeBoolean(user != null)
        user?.writeTo(out)
    }

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf<String, Any?>(
            _ID to id,
            ENTITY_ID_FIELD to entityId,
            COMMENT_CONTENT_FIELD to content,
            COMMENT_CREATED_TIME_FIELD to createdTime,
            COMMENT_LAST_UPDATED_TIME_FIELD to lastUpdatedTime,
            COMMENT_USER_FIELD to user?.name
        )
    }

    // used to create the Comment JSON object for an API response (displayed to user)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, false)
    }

    // used to create the Comment JSON object for indexing a doc into an index (not displayed to user)
    fun toXContentWithUser(builder: XContentBuilder): XContentBuilder {
        return createXContentBuilder(builder, true)
    }

    private fun createXContentBuilder(builder: XContentBuilder, includeFullUser: Boolean): XContentBuilder {
        builder.startObject()
            .field(ENTITY_ID_FIELD, entityId)
            .field(COMMENT_CONTENT_FIELD, content)
            .optionalTimeField(COMMENT_CREATED_TIME_FIELD, createdTime)
            .optionalTimeField(COMMENT_LAST_UPDATED_TIME_FIELD, lastUpdatedTime)

        if (includeFullUser) {
            // if we're storing a Comment into an internal index, include full User
            builder.optionalUserField(COMMENT_USER_FIELD, user)
        } else {
            // if we're displaying the Comment as part of an API call response, only include username
            builder.optionalUsernameField(COMMENT_USER_FIELD, user)
        }

        builder.endObject()
        return builder
    }

    companion object {
        const val ENTITY_ID_FIELD = "entity_id"
        const val COMMENT_CONTENT_FIELD = "content"
        const val COMMENT_CREATED_TIME_FIELD = "created_time"
        const val COMMENT_LAST_UPDATED_TIME_FIELD = "last_updated_time"
        const val COMMENT_USER_FIELD = "user"
        const val NO_ID = ""

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID): Comment {
            lateinit var entityId: String
            var content = ""
            lateinit var createdTime: Instant
            var lastUpdatedTime: Instant? = null
            var user: User? = null

            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ENTITY_ID_FIELD -> entityId = xcp.text()
                    COMMENT_CONTENT_FIELD -> content = xcp.text()
                    COMMENT_CREATED_TIME_FIELD -> createdTime = requireNotNull(xcp.instant())
                    COMMENT_LAST_UPDATED_TIME_FIELD -> lastUpdatedTime = xcp.instant()
                    COMMENT_USER_FIELD ->
                        user = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) {
                            null
                        } else {
                            User.parse(xcp)
                        }
                }
            }

            return Comment(
                id = id,
                entityId = entityId,
                content = content,
                createdTime = createdTime,
                lastUpdatedTime = lastUpdatedTime,
                user = user
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Comment {
            return Comment(sin)
        }
    }
}
