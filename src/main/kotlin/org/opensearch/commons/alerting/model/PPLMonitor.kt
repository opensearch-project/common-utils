package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.apache.logging.log4j.LogManager
import org.opensearch.commons.alerting.model.MonitorV2.Companion.ENABLED_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.ENABLED_TIME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.LAST_UPDATE_TIME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.MONITOR_TYPE_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.MONITOR_V2_TYPE
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NO_ID
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NO_VERSION
import org.opensearch.commons.alerting.model.MonitorV2.Companion.SCHEDULE_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.TRIGGERS_FIELD
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.nonOptionalTimeField
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

private val logger = LogManager.getLogger(PPLMonitor::class.java)

// TODO: probably change this to be called PPLSQLMonitor. A PPL Monitor and SQL Monitor
// TODO: would have the exact same functionality, except the choice of language
// TODO: when calling PPL/SQL plugin's execute API would be different.
// TODO: we dont need 2 different monitor types for that, just a simple if check
// TODO: for query language at monitor execution time
data class PPLMonitor(
    override val id: String = NO_ID,
    override val version: Long = NO_VERSION,
    override val name: String,
    override val enabled: Boolean,
    override val schedule: Schedule,
    override val lastUpdateTime: Instant,
    override val enabledTime: Instant?,
    override val triggers: List<TriggerV2>,
    val queryLanguage: QueryLanguage = QueryLanguage.PPL, // default to PPL, SQL not currently supported
    val query: String
) : MonitorV2 {

    // specify scheduled job type
    override val type = MONITOR_V2_TYPE

    override fun fromDocument(id: String, version: Long): PPLMonitor = copy(id = id, version = version)

    init {
        // SQL monitors are not yet supported
        if (this.queryLanguage == QueryLanguage.SQL) {
            throw IllegalStateException("Monitors with SQL queries are not supported")
        }

        // for checking trigger ID uniqueness
        val triggerIds = mutableSetOf<String>()
        triggers.forEach { trigger ->
            require(triggerIds.add(trigger.id)) { "Duplicate trigger id: ${trigger.id}. Trigger ids must be unique." }
        }

        if (enabled) {
            requireNotNull(enabledTime)
        } else {
            require(enabledTime == null)
        }

        triggers.forEach { trigger ->
            require(trigger is PPLTrigger) { "Incompatible trigger [${trigger.id}] for monitor type [$PPL_MONITOR_TYPE]" }
        }

        // TODO: create setting for max triggers and check for max triggers here
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        version = sin.readLong(),
        name = sin.readString(),
        enabled = sin.readBoolean(),
        schedule = Schedule.readFrom(sin),
        lastUpdateTime = sin.readInstant(),
        enabledTime = sin.readOptionalInstant(),
        triggers = sin.readList(TriggerV2::readFrom),
        queryLanguage = sin.readEnum(QueryLanguage::class.java),
        query = sin.readString()
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject() // overall start object

        // if this is being written as ScheduledJob, add extra object layer and add ScheduledJob
        // related metadata, default to false
        if (params.paramAsBoolean("with_type", false)) {
            builder.startObject(MONITOR_V2_TYPE)
        }

        // wrap PPLMonitor in outer object named after its monitor type
        // required for MonitorV2 XContentParser to first encounter this,
        // read in monitor type, then delegate to correct parse() function
        builder.startObject(PPL_MONITOR_TYPE) // monitor type start object

        builder.field(NAME_FIELD, name)
        builder.field(SCHEDULE_FIELD, schedule)
        builder.field(ENABLED_FIELD, enabled)
        builder.optionalTimeField(ENABLED_TIME_FIELD, enabledTime)
        builder.nonOptionalTimeField(LAST_UPDATE_TIME_FIELD, lastUpdateTime)
        builder.field(TRIGGERS_FIELD, triggers.toTypedArray())
        builder.field(QUERY_LANGUAGE_FIELD, queryLanguage.value)
        builder.field(QUERY_FIELD, query)

        builder.endObject() // monitor type end object

        // if ScheduledJob metadata was added, end the extra object layer that was created
        if (params.paramAsBoolean("with_type", false)) {
            builder.endObject()
        }

        builder.endObject() // overall end object

        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeString(name)
        out.writeBoolean(enabled)
        if (schedule is CronSchedule) {
            out.writeEnum(Schedule.TYPE.CRON)
        } else {
            out.writeEnum(Schedule.TYPE.INTERVAL)
        }
        out.writeInstant(lastUpdateTime)
        out.writeOptionalInstant(enabledTime)
        out.writeVInt(triggers.size)
        triggers.forEach {
            out.writeEnum(TriggerV2.TriggerV2Type.PPL_TRIGGER)
            it.writeTo(out)
        }
        out.writeEnum(queryLanguage)
        out.writeString(query)
    }

    override fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            _ID to id,
            _VERSION to version,
            NAME_FIELD to name,
            ENABLED_FIELD to enabled,
            SCHEDULE_FIELD to schedule,
            LAST_UPDATE_TIME_FIELD to lastUpdateTime.toEpochMilli(),
            ENABLED_TIME_FIELD to enabledTime?.toEpochMilli(),
            TRIGGERS_FIELD to triggers,
            QUERY_LANGUAGE_FIELD to queryLanguage.value,
            QUERY_FIELD to query
        )
    }

    enum class QueryLanguage(val value: String) {
        PPL(PPL_QUERY_LANGUAGE),
        SQL(SQL_QUERY_LANGUAGE);

        companion object {
            fun enumFromString(value: String): QueryLanguage? = QueryLanguage.entries.firstOrNull { it.value == value }
        }
    }

    companion object {
        // monitor type name
        const val PPL_MONITOR_TYPE = "ppl_monitor" // TODO: eventually change to SQL_PPL_MONITOR_TYPE

        // query languages
        const val PPL_QUERY_LANGUAGE = "ppl"
        const val SQL_QUERY_LANGUAGE = "sql"

        // field names
        const val QUERY_LANGUAGE_FIELD = "query_language"
        const val QUERY_FIELD = "query"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): PPLMonitor {
            var name: String? = null
            var monitorType: String = PPL_MONITOR_TYPE
            var enabled = true
            var schedule: Schedule? = null
            var lastUpdateTime: Instant? = null
            var enabledTime: Instant? = null
            val triggers: MutableList<TriggerV2> = mutableListOf()
            var queryLanguage: QueryLanguage = QueryLanguage.PPL // default to PPL
            var query: String? = null


            /* parse */
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    NAME_FIELD -> name = xcp.text()
                    MONITOR_TYPE_FIELD -> monitorType = xcp.text()
                    ENABLED_FIELD -> enabled = xcp.booleanValue()
                    SCHEDULE_FIELD -> schedule = Schedule.parse(xcp)
                    ENABLED_TIME_FIELD -> enabledTime = xcp.instant()
                    LAST_UPDATE_TIME_FIELD -> lastUpdateTime = xcp.instant()
                    TRIGGERS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            triggers.add(PPLTrigger.parseInner(xcp))
                        }
                    }
                    QUERY_LANGUAGE_FIELD -> {
                        val input = xcp.text()
                        val enumMatchResult = QueryLanguage.enumFromString(input)
                            ?: throw IllegalArgumentException("Invalid value for $QUERY_LANGUAGE_FIELD: $input. Supported values are ${QueryLanguage.entries.map { it.value }}")
                        queryLanguage = enumMatchResult
                    }
                    QUERY_FIELD -> query = xcp.text()
                    else -> throw IllegalArgumentException("Unexpected field \"$fieldName\" when parsing PPL Monitor")
                }
            }

            /* validations */

            // TODO: add validations for throttle actions time range
            // (see alerting's TransportIndexMonitorAction.validateActionThrottle)

            // ensure MonitorV2 XContent being parsed by PPLMonitor class is PPL Monitor type
            if (monitorType != PPL_MONITOR_TYPE) {
                throw IllegalArgumentException("Invalid monitor type: $monitorType")
            }

            // ensure there's at least 1 trigger
            if (triggers.isEmpty()) {
                throw IllegalArgumentException("Monitor must include at least 1 trigger")
            }

            // if enabled, set time of MonitorV2 creation/update is set as enable time
            if (enabled && enabledTime == null) {
                enabledTime = Instant.now()
            } else if (!enabled) {
                enabledTime = null
            }

            lastUpdateTime = lastUpdateTime ?: Instant.now()

            // check for required fields
            requireNotNull(name) { "Monitor name is null" }
            requireNotNull(schedule) { "Schedule is null" }
            requireNotNull(queryLanguage) { "Query language is null" }
            requireNotNull(query) { "Query is null" }
            requireNotNull(lastUpdateTime) { "Last update time is null" }

            if (queryLanguage == QueryLanguage.SQL) {
                throw IllegalArgumentException("SQL queries are not supported. Please use a PPL query.")
            }

            /* return PPLMonitor */
            return PPLMonitor(
                id,
                version,
                name,
                enabled,
                schedule,
                lastUpdateTime,
                enabledTime,
                triggers,
                queryLanguage,
                query
            )
        }
    }
}
