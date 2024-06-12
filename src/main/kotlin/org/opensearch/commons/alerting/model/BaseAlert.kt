package org.opensearch.commons.alerting.model

import org.opensearch.common.lucene.uid.Versions
import org.opensearch.commons.alerting.util.IndexUtils.Companion.NO_SCHEMA_VERSION
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalUserField
import org.opensearch.commons.authuser.User
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.time.Instant

/** CorrelationAlert and Alert can extend the UnifiedAlert class to inherit the common fields and behavior
 * of UnifiedAlert class.
 */
open class BaseAlert(
    open val id: String = Alert.NO_ID,
    open val version: Long = Alert.NO_VERSION,
    open val schemaVersion: Int = NO_SCHEMA_VERSION,
    open val user: User?,
    open val triggerName: String,

    // State will be later moved to this Class  (after `monitorBasedAlerts` extend this Class)
    open val state: Alert.State,
    open val startTime: Instant,
    open val endTime: Instant? = null,
    open val acknowledgedTime: Instant? = null,
    open val errorMessage: String? = null,
    open val severity: String,
    open val actionExecutionResults: List<ActionExecutionResult>
) : Writeable, ToXContent {

    init {
        if (errorMessage != null) {
            require((state == Alert.State.DELETED) || (state == Alert.State.ERROR) || (state == Alert.State.AUDIT)) {
                "Attempt to create an alert with an error in state: $state"
            }
        }
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        version = sin.readLong(),
        schemaVersion = sin.readInt(),
        user = if (sin.readBoolean()) {
            User(sin)
        } else {
            null
        },
        triggerName = sin.readString(),
        state = sin.readEnum(Alert.State::class.java),
        startTime = sin.readInstant(),
        endTime = sin.readOptionalInstant(),
        acknowledgedTime = sin.readOptionalInstant(),
        errorMessage = sin.readOptionalString(),
        severity = sin.readString(),
        actionExecutionResults = sin.readList(::ActionExecutionResult)
    )

    fun isAcknowledged(): Boolean = (state == Alert.State.ACKNOWLEDGED)

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeInt(schemaVersion)
        out.writeBoolean(user != null)
        user?.writeTo(out)
        out.writeString(triggerName)
        out.writeEnum(state)
        out.writeInstant(startTime)
        out.writeOptionalInstant(endTime)
        out.writeOptionalInstant(acknowledgedTime)
        out.writeOptionalString(errorMessage)
        out.writeString(severity)
        out.writeCollection(actionExecutionResults)
    }

    companion object {
        const val ALERT_ID_FIELD = "id"
        const val SCHEMA_VERSION_FIELD = "schema_version"
        const val ALERT_VERSION_FIELD = "version"
        const val USER_FIELD = "user"
        const val TRIGGER_NAME_FIELD = "trigger_name"
        const val STATE_FIELD = "state"
        const val START_TIME_FIELD = "start_time"
        const val END_TIME_FIELD = "end_time"
        const val ACKNOWLEDGED_TIME_FIELD = "acknowledged_time"
        const val ERROR_MESSAGE_FIELD = "error_message"
        const val SEVERITY_FIELD = "severity"
        const val ACTION_EXECUTION_RESULTS_FIELD = "action_execution_results"
        const val NO_ID = ""
        const val NO_VERSION = Versions.NOT_FOUND

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, version: Long = NO_VERSION): BaseAlert {
            lateinit var id: String
            var schemaVersion = NO_SCHEMA_VERSION
            var version: Long = Versions.NOT_FOUND
            var user: User? = null
            lateinit var triggerName: String
            lateinit var state: Alert.State
            lateinit var startTime: Instant
            lateinit var severity: String
            var endTime: Instant? = null
            var acknowledgedTime: Instant? = null
            var errorMessage: String? = null
            val actionExecutionResults: MutableList<ActionExecutionResult> = mutableListOf()
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    USER_FIELD -> user = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) null else User.parse(xcp)
                    ALERT_ID_FIELD -> id = xcp.text()
                    ALERT_VERSION_FIELD -> version = xcp.longValue()
                    SCHEMA_VERSION_FIELD -> schemaVersion = xcp.intValue()
                    TRIGGER_NAME_FIELD -> triggerName = xcp.text()
                    STATE_FIELD -> state = Alert.State.valueOf(xcp.text())
                    ERROR_MESSAGE_FIELD -> errorMessage = xcp.textOrNull()
                    SEVERITY_FIELD -> severity = xcp.text()
                    ACTION_EXECUTION_RESULTS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actionExecutionResults.add(ActionExecutionResult.parse(xcp))
                        }
                    }
                    START_TIME_FIELD -> startTime = requireNotNull(xcp.instant())
                    END_TIME_FIELD -> endTime = requireNotNull(xcp.instant())
                    ACKNOWLEDGED_TIME_FIELD -> acknowledgedTime = xcp.instant()
                }
            }

            return BaseAlert(
                id = id,
                startTime = requireNotNull(startTime),
                endTime = endTime,
                state = requireNotNull(state),
                version = version,
                errorMessage = errorMessage,
                actionExecutionResults = actionExecutionResults,
                schemaVersion = schemaVersion,
                user = user,
                triggerName = requireNotNull(triggerName),
                severity = severity,
                acknowledgedTime = acknowledgedTime
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Alert {
            return Alert(sin)
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, true)
    }

    fun toXContentWithUser(builder: XContentBuilder): XContentBuilder {
        return createXContentBuilder(builder, false)
    }

    fun createXContentBuilder(builder: XContentBuilder, secure: Boolean): XContentBuilder {
        if (!secure) {
            builder.optionalUserField(USER_FIELD, user)
        }
        builder
            .field(ALERT_ID_FIELD, id)
            .field(ALERT_VERSION_FIELD, version)
            .field(SCHEMA_VERSION_FIELD, schemaVersion)
            .field(TRIGGER_NAME_FIELD, triggerName)
            .field(STATE_FIELD, state)
            .field(ERROR_MESSAGE_FIELD, errorMessage)
            .field(SEVERITY_FIELD, severity)
            .field(ACTION_EXECUTION_RESULTS_FIELD, actionExecutionResults.toTypedArray())
            .field(START_TIME_FIELD, startTime)
            .field(END_TIME_FIELD, endTime)
            .field(ACKNOWLEDGED_TIME_FIELD, acknowledgedTime)
        return builder
    }

    open fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            ACKNOWLEDGED_TIME_FIELD to acknowledgedTime?.toEpochMilli(),
            ALERT_ID_FIELD to id,
            ALERT_VERSION_FIELD to version,
            END_TIME_FIELD to endTime?.toEpochMilli(),
            ERROR_MESSAGE_FIELD to errorMessage,
            SEVERITY_FIELD to severity,
            START_TIME_FIELD to startTime.toEpochMilli(),
            STATE_FIELD to state.toString(),
            TRIGGER_NAME_FIELD to triggerName
        )
    }
}
