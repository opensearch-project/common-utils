package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.apache.logging.log4j.LogManager
import org.opensearch.Version
import org.opensearch.common.CheckedFunction
import org.opensearch.commons.alerting.model.Monitor.Companion
import org.opensearch.commons.alerting.model.Monitor.Companion.MONITOR_TYPE
import org.opensearch.commons.alerting.model.MonitorV2.Companion.ENABLED_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.ENABLED_TIME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.LABELS_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.LAST_UPDATE_TIME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.MONITOR_TYPE_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.MONITOR_V2_TYPE
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NO_ID
import org.opensearch.commons.alerting.model.MonitorV2.Companion.NO_VERSION
import org.opensearch.commons.alerting.model.MonitorV2.Companion.SCHEDULE_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.TRIGGERS_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.TYPE_FIELD
import org.opensearch.commons.alerting.model.MonitorV2.Companion.convertLabelsMap
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.nonOptionalTimeField
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

private val logger = LogManager.getLogger(PPLMonitor::class.java)

data class PPLMonitor(
    override val id: String = NO_ID,
    override val version: Long = NO_VERSION,
    override val name: String,
    override val enabled: Boolean,
    override val schedule: Schedule,
    override val lastUpdateTime: Instant,
    override val enabledTime: Instant?,
    override val labels: Map<String, String> = emptyMap(),
    override val triggers: List<TriggerV2>,
    val query: String
) : MonitorV2 {

    // specify scheduled job type
    override val type = MONITOR_V2_TYPE

    override fun fromDocument(id: String, version: Long): PPLMonitor = copy(id = id, version = version)

    init {
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
        labels = sin.readMap()?.let { convertLabelsMap(it) } ?: emptyMap(),
        triggers = sin.readList(TriggerV2::readFrom),
        query = sin.readString()
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()

        // if this is being written as ScheduledJob, add extra object layer and add ScheduledJob
        // related metadata, default to false
        if (params.paramAsBoolean("with_type", false)) {
            builder.startObject(MONITOR_V2_TYPE)
        }
        builder.field(TYPE_FIELD, MONITOR_V2_TYPE)

        // include monitor type field despite it not being a class field to differentiate
        // PPL monitor from other monitor types in alerting config system index
        builder.field(MONITOR_TYPE_FIELD, PPL_MONITOR_TYPE)

        builder.field(NAME_FIELD, name)
        builder.field(SCHEDULE_FIELD, schedule)
        builder.field(ENABLED_FIELD, enabled)
        builder.optionalTimeField(ENABLED_TIME_FIELD, enabledTime)
        builder.nonOptionalTimeField(LAST_UPDATE_TIME_FIELD, lastUpdateTime)
        builder.field(LABELS_FIELD, labels)
        builder.field(TRIGGERS_FIELD, triggers.toTypedArray())
        builder.field(QUERY_FIELD, query)
        builder.endObject()

        // if ScheduledJob metadata was added, end the extra object layer that was created
        if (params.paramAsBoolean("with_type", false)) {
            builder.endObject()
        }

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
        out.writeMap(labels)
        out.writeVInt(triggers.size)
        triggers.forEach {
            out.writeEnum(TriggerV2.TriggerV2Type.PPL_TRIGGER)
            it.writeTo(out)
        }
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
            LABELS_FIELD to labels,
            TRIGGERS_FIELD to triggers,
            QUERY_FIELD to query
        )
    }

    companion object {
        // monitor type name
        const val PPL_MONITOR_TYPE = "ppl_monitor"

        // field names
        const val QUERY_FIELD = "query"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): PPLMonitor {
            var name: String? = null
            var monitorType: String = PPL_MONITOR_TYPE
            var enabled = true
            var schedule: Schedule? = null
            val lastUpdateTime: Instant = Instant.now() // set time of update or first creation as lastUpdateTime
            var enabledTime: Instant? = null
            var labels: Map<String, Any> = emptyMap()
            val triggers: MutableList<TriggerV2> = mutableListOf()
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
                    LABELS_FIELD -> labels = xcp.map()
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
                    QUERY_FIELD -> query = xcp.text()
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

            // check if all label key,values are String,String, throw exception otherwise
            try {
                labels = convertLabelsMap(labels)
            } catch (e: ClassCastException) {
                throw IllegalArgumentException("invalid maps field, please ensure all labels are strings")
            }

            // check for required fields
            requireNotNull(name) { "Monitor name is null" }
            requireNotNull(schedule) { "Schedule is null" }
            requireNotNull(query) { "Query is null" }

            /* return PPLMonitor */
            return PPLMonitor(
                id,
                version,
                name,
                enabled,
                schedule,
                lastUpdateTime,
                enabledTime,
                labels,
                triggers,
                query
            )
        }

//        @JvmStatic
//        @Throws(IOException::class)
//        fun readFrom(sin: StreamInput): PPLMonitor {
//            return PPLMonitor(sin)
//        }
    }
}
