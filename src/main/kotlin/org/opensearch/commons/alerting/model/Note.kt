package org.opensearch.commons.alerting.model

import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.commons.alerting.util.optionalUsernameField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken
import java.io.IOException
import java.time.Instant
import java.util.Collections
import org.apache.logging.log4j.LogManager
import org.opensearch.commons.alerting.util.optionalUserField
import org.opensearch.commons.authuser.User

private val log = LogManager.getLogger(Note::class.java)

data class Note(
    val id: String = NO_ID,
    val alertId: String = Alert.NO_ID,
    val content: String,
    val time: Instant,
    val user: User?
) : Writeable, ToXContent {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        alertId = sin.readString(),
        content = sin.readString(),
        time = sin.readInstant(),
        user = if (sin.readBoolean()) {
            User(sin)
        } else {
            null
        }
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(alertId)
        out.writeString(content)
        out.writeInstant(time)
        out.writeBoolean(user != null)
        user?.writeTo(out)
    }

    fun asTemplateArg(): Map<String, Any?> {
        val templateArgMap = mutableMapOf(
            _ID to id,
            ALERT_ID_FIELD to alertId,
            NOTE_CONTENT_FIELD to content,
            NOTE_TIME_FIELD to time
        )
        if (user != null) {
            templateArgMap[NOTE_USER_FIELD] = user.name
        }
        return Collections.unmodifiableMap(templateArgMap)
    }

    // used to create the Note JSON object for an API response (displayed to user)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, false)
    }

    // used to create the Note JSON object for indexing a doc into an index (not displayed to user)
    fun toXContentWithUser(builder: XContentBuilder): XContentBuilder {
        return createXContentBuilder(builder, true)
    }

    private fun createXContentBuilder(builder: XContentBuilder, includeFullUser: Boolean): XContentBuilder {
        log.info("include full user: $includeFullUser")
        builder.startObject()
            .field(ALERT_ID_FIELD, alertId)
            .field(NOTE_CONTENT_FIELD, content)
            .optionalTimeField(NOTE_TIME_FIELD, time)

        if (includeFullUser) {
            // if we're storing a Note into an internal index, include full User
            builder.optionalUserField(NOTE_USER_FIELD, user)
        } else {
            // if we're displaying the Note as part of an API call response, only include username
            builder.optionalUsernameField(NOTE_USER_FIELD, user)
        }

        builder.endObject()
        return builder
    }

    companion object {
        const val ALERT_ID_FIELD = "alert_id"
        const val NOTE_CONTENT_FIELD = "content"
        const val NOTE_TIME_FIELD = "time"
        const val NOTE_USER_FIELD = "user"
        const val NO_ID = ""

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID): Note {
            lateinit var alertId: String
            var content = ""
            lateinit var time: Instant
            var user: User? = null

            log.info("note parse curr token: ${xcp.currentToken()}")
            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ALERT_ID_FIELD -> alertId = xcp.text()
                    NOTE_CONTENT_FIELD -> content = xcp.text()
                    NOTE_TIME_FIELD -> time = requireNotNull(xcp.instant())
                    NOTE_USER_FIELD ->
                        user = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) {
                            null
                        } else {
                            User.parse(xcp)
                        }
                }
            }

            return Note(
                id = id,
                alertId = requireNotNull(alertId) { "Alert ID is null" },
                content = content,
                time = requireNotNull(time),
                user = user
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Note {
            return Note(sin)
        }
    }
}
