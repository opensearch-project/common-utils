package org.opensearch.commons.alerting.model

import org.opensearch.common.lucene.uid.Versions
import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.commons.alerting.model.Alert.Companion.ACKNOWLEDGED_TIME_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.ACTION_EXECUTION_RESULTS_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.ALERT_HISTORY_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.ALERT_ID_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.ALERT_VERSION_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.END_TIME_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.ERROR_MESSAGE_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.EXECUTION_ID_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.LAST_NOTIFICATION_TIME_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.MONITOR_ID_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.MONITOR_NAME_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.MONITOR_VERSION_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.NO_ID
import org.opensearch.commons.alerting.model.Alert.Companion.NO_VERSION
import org.opensearch.commons.alerting.model.Alert.Companion.SCHEMA_VERSION_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.SEVERITY_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.START_TIME_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.STATE_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.TRIGGER_ID_FIELD
import org.opensearch.commons.alerting.model.Alert.Companion.TRIGGER_NAME_FIELD
import org.opensearch.commons.alerting.model.Alert.State
import org.opensearch.commons.alerting.util.IndexUtils.Companion.NO_SCHEMA_VERSION
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken
import java.io.IOException
import java.time.Instant

data class AlertV2(
    val id: String = NO_ID,
    val version: Long = NO_VERSION,
    val schemaVersion: Int = NO_SCHEMA_VERSION,
    val monitorId: String,
    val monitorName: String,
    val monitorVersion: Long,
//    val monitorUser: User?,
    val triggerId: String,
    val triggerName: String,
    val queryResults: Map<String, Any>,
    val state: State, // TODO: potentially delete for stateless alerts, all stateless alerts are ACTIVE
    val startTime: Instant, // TODO: potentially delete for stateless alerts
    val endTime: Instant? = null, // TODO: potentially delete for stateless alerts
    val expirationTime: Instant?,
    val lastNotificationTime: Instant? = null,
    val acknowledgedTime: Instant? = null,
    val errorMessage: String? = null,
    val errorHistory: List<AlertError>,
    val severity: String,
    val actionExecutionResults: List<ActionExecutionResult>,
    val executionId: String? = null
) : Writeable, ToXContent {
    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        version = sin.readLong(),
        schemaVersion = sin.readInt(),
        monitorId = sin.readString(),
        monitorName = sin.readString(),
        monitorVersion = sin.readLong(),
//        monitorUser = if (sin.readBoolean()) {
//            User(sin)
//        } else {
//            null
//        },
        triggerId = sin.readString(),
        triggerName = sin.readString(),
        queryResults = sin.readMap()!!.toMap(),
        state = sin.readEnum(State::class.java),
        startTime = sin.readInstant(),
        endTime = sin.readOptionalInstant(),
        expirationTime = sin.readOptionalInstant(),
        lastNotificationTime = sin.readOptionalInstant(),
        acknowledgedTime = sin.readOptionalInstant(),
        errorMessage = sin.readOptionalString(),
        errorHistory = sin.readList(::AlertError),
        severity = sin.readString(),
        actionExecutionResults = sin.readList(::ActionExecutionResult),
        executionId = sin.readOptionalString()
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeInt(schemaVersion)
        out.writeString(monitorId)
        out.writeString(monitorName)
        out.writeLong(monitorVersion)
//        out.writeBoolean(monitorUser != null)
//        monitorUser?.writeTo(out)
        out.writeString(triggerId)
        out.writeString(triggerName)
        out.writeMap(queryResults)
        out.writeEnum(state)
        out.writeInstant(startTime)
        out.writeOptionalInstant(endTime)
        out.writeOptionalInstant(expirationTime)
        out.writeOptionalInstant(lastNotificationTime)
        out.writeOptionalInstant(acknowledgedTime)
        out.writeOptionalString(errorMessage)
        out.writeCollection(errorHistory)
        out.writeString(severity)
        out.writeCollection(actionExecutionResults)
        out.writeOptionalString(executionId)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(ALERT_ID_FIELD, id)
            .field(ALERT_VERSION_FIELD, version)
            .field(MONITOR_ID_FIELD, monitorId)
            .field(SCHEMA_VERSION_FIELD, schemaVersion)
            .field(MONITOR_VERSION_FIELD, monitorVersion)
            .field(MONITOR_NAME_FIELD, monitorName)
            .field(EXECUTION_ID_FIELD, executionId)
            .field(TRIGGER_ID_FIELD, triggerId)
            .field(TRIGGER_NAME_FIELD, triggerName)
            .field(QUERY_RESULTS_FIELD, queryResults)
            .field(STATE_FIELD, state)
            .field(ERROR_MESSAGE_FIELD, errorMessage)
            .field(ALERT_HISTORY_FIELD, errorHistory.toTypedArray())
            .field(SEVERITY_FIELD, severity)
            .field(ACTION_EXECUTION_RESULTS_FIELD, actionExecutionResults.toTypedArray())
            .optionalTimeField(EXPIRATION_TIME_FIELD, expirationTime)
            .optionalTimeField(START_TIME_FIELD, startTime)
            .optionalTimeField(LAST_NOTIFICATION_TIME_FIELD, lastNotificationTime)
            .optionalTimeField(END_TIME_FIELD, endTime)
            .optionalTimeField(ACKNOWLEDGED_TIME_FIELD, acknowledgedTime)
            .endObject()

//        if (!secure) {
//            builder.optionalUserField(MONITOR_USER_FIELD, monitorUser)
//        }

        return builder
    }

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            ACKNOWLEDGED_TIME_FIELD to acknowledgedTime?.toEpochMilli(),
            ALERT_ID_FIELD to id,
            ALERT_VERSION_FIELD to version,
            END_TIME_FIELD to endTime?.toEpochMilli(),
            ERROR_MESSAGE_FIELD to errorMessage,
            EXECUTION_ID_FIELD to executionId,
            LAST_NOTIFICATION_TIME_FIELD to lastNotificationTime?.toEpochMilli(),
            EXPIRATION_TIME_FIELD to expirationTime?.toEpochMilli(),
            SEVERITY_FIELD to severity,
            START_TIME_FIELD to startTime.toEpochMilli(),
            STATE_FIELD to state.toString()
        )
    }

    companion object {
        const val EXPIRATION_TIME_FIELD = "expiration_time"
        const val QUERY_RESULTS_FIELD = "query_results"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): AlertV2 {
            var schemaVersion = NO_SCHEMA_VERSION
            lateinit var monitorId: String
            lateinit var monitorName: String
            var monitorVersion: Long = Versions.NOT_FOUND
//            var monitorUser: User? = null
            lateinit var triggerId: String
            lateinit var triggerName: String
            var queryResults: Map<String, Any> = mapOf()
            lateinit var state: State
            lateinit var startTime: Instant
            lateinit var severity: String
            var expirationTime: Instant? = null
            var endTime: Instant? = null
            var lastNotificationTime: Instant? = null
            var acknowledgedTime: Instant? = null
            var errorMessage: String? = null
            var executionId: String? = null
            val errorHistory: MutableList<AlertError> = mutableListOf()
            val actionExecutionResults: MutableList<ActionExecutionResult> = mutableListOf()

            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    MONITOR_ID_FIELD -> monitorId = xcp.text()
                    SCHEMA_VERSION_FIELD -> schemaVersion = xcp.intValue()
                    MONITOR_NAME_FIELD -> monitorName = xcp.text()
                    MONITOR_VERSION_FIELD -> monitorVersion = xcp.longValue()
//                    MONITOR_USER_FIELD ->
//                        monitorUser = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) {
//                            null
//                        } else {
//                            User.parse(xcp)
//                        }
                    TRIGGER_ID_FIELD -> triggerId = xcp.text()
                    STATE_FIELD -> state = State.valueOf(xcp.text())
                    TRIGGER_NAME_FIELD -> triggerName = xcp.text()
                    QUERY_RESULTS_FIELD -> queryResults = xcp.map()
                    START_TIME_FIELD -> startTime = requireNotNull(xcp.instant())
                    END_TIME_FIELD -> endTime = xcp.instant()
                    EXPIRATION_TIME_FIELD -> expirationTime = xcp.instant()
                    LAST_NOTIFICATION_TIME_FIELD -> lastNotificationTime = xcp.instant()
                    ACKNOWLEDGED_TIME_FIELD -> acknowledgedTime = xcp.instant()
                    ERROR_MESSAGE_FIELD -> errorMessage = xcp.textOrNull()
                    EXECUTION_ID_FIELD -> executionId = xcp.textOrNull()
                    ALERT_HISTORY_FIELD -> {
                        ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            errorHistory.add(AlertError.parse(xcp))
                        }
                    }
                    SEVERITY_FIELD -> severity = xcp.text()
                    ACTION_EXECUTION_RESULTS_FIELD -> {
                        ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actionExecutionResults.add(ActionExecutionResult.parse(xcp))
                        }
                    }
                }
            }

            return AlertV2(
                id = id,
                version = version,
                schemaVersion = schemaVersion,
                monitorId = requireNotNull(monitorId),
                monitorName = requireNotNull(monitorName),
                monitorVersion = monitorVersion,
//                monitorUser = monitorUser,
                triggerId = requireNotNull(triggerId),
                triggerName = requireNotNull(triggerName),
                queryResults = requireNotNull(queryResults),
                state = requireNotNull(state),
                startTime = requireNotNull(startTime),
                endTime = endTime,
                expirationTime = expirationTime,
                lastNotificationTime = lastNotificationTime,
                acknowledgedTime = acknowledgedTime,
                errorMessage = errorMessage,
                errorHistory = errorHistory,
                severity = severity,
                actionExecutionResults = actionExecutionResults,
                executionId = executionId
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): AlertV2 {
            return AlertV2(sin)
        }
    }
}
