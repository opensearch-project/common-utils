package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.commons.alerting.util.IndexUtils.Companion.MONITOR_MAX_INPUTS
import org.opensearch.commons.alerting.util.IndexUtils.Companion.MONITOR_MAX_TRIGGERS
import org.opensearch.commons.alerting.util.IndexUtils.Companion.NO_SCHEMA_VERSION
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.alerting.util.IndexUtils.Companion.supportedClusterMetricsSettings
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.isBucketLevelMonitor
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.commons.alerting.util.optionalUserField
import org.opensearch.commons.authuser.User
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.time.Instant
import java.util.regex.Pattern

data class Monitor(
    override val id: String = NO_ID,
    override val version: Long = NO_VERSION,
    override val name: String,
    override val enabled: Boolean,
    override val schedule: Schedule,
    override val lastUpdateTime: Instant,
    override val enabledTime: Instant?,
    // TODO: Check how this behaves during rolling upgrade/multi-version cluster
    //  Can read/write and parsing break if it's done from an old -> new version of the plugin?
    val monitorType: String,
    val user: User?,
    val schemaVersion: Int = NO_SCHEMA_VERSION,
    val inputs: List<Input>,
    val triggers: List<Trigger>,
    val uiMetadata: Map<String, Any>,
    val dataSources: DataSources = DataSources(),
    val owner: String? = "alerting"
) : ScheduledJob {

    override val type = MONITOR_TYPE

    init {
        // Ensure that trigger ids are unique within a monitor
        val triggerIds = mutableSetOf<String>()
        triggers.forEach { trigger ->
            // NoOpTrigger is only used in "Monitor Error Alerts" as a placeholder
            require(trigger !is NoOpTrigger)

            require(triggerIds.add(trigger.id)) { "Duplicate trigger id: ${trigger.id}. Trigger ids must be unique." }
            // Verify Trigger type based on Monitor type
            when (monitorType) {
                MonitorType.QUERY_LEVEL_MONITOR.value ->
                    require(trigger is QueryLevelTrigger) { "Incompatible trigger [${trigger.id}] for monitor type [$monitorType]" }
                MonitorType.BUCKET_LEVEL_MONITOR.value ->
                    require(trigger is BucketLevelTrigger) { "Incompatible trigger [${trigger.id}] for monitor type [$monitorType]" }
                MonitorType.CLUSTER_METRICS_MONITOR.value ->
                    require(trigger is QueryLevelTrigger) { "Incompatible trigger [${trigger.id}] for monitor type [$monitorType]" }
                MonitorType.DOC_LEVEL_MONITOR.value ->
                    require(trigger is DocumentLevelTrigger) { "Incompatible trigger [${trigger.id}] for monitor type [$monitorType]" }
            }
        }
        if (enabled) {
            requireNotNull(enabledTime)
        } else {
            require(enabledTime == null)
        }
        require(inputs.size <= MONITOR_MAX_INPUTS) { "Monitors can only have $MONITOR_MAX_INPUTS search input." }
        require(triggers.size <= MONITOR_MAX_TRIGGERS) { "Monitors can only support up to $MONITOR_MAX_TRIGGERS triggers." }
        if (this.isBucketLevelMonitor()) {
            inputs.forEach { input ->
                require(input is SearchInput) { "Unsupported input [$input] for Monitor" }
                // TODO: Keeping query validation simple for now, only term aggregations have full support for the "group by" on the
                //  initial release. Should either add tests for other aggregation types or add validation to prevent using them.
                require(input.query.aggregations() != null && !input.query.aggregations().aggregatorFactories.isEmpty()) {
                    "At least one aggregation is required for the input [$input]"
                }
            }
        }
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
        monitorType = sin.readString(),
        user = if (sin.readBoolean()) {
            User(sin)
        } else {
            null
        },
        schemaVersion = sin.readInt(),
        inputs = sin.readList((Input)::readFrom),
        triggers = sin.readList((Trigger)::readFrom),
        uiMetadata = suppressWarning(sin.readMap()),
        dataSources = if (sin.readBoolean()) {
            DataSources(sin)
        } else {
            DataSources()
        },
        owner = sin.readOptionalString()
    )

    // This enum classifies different Monitors
    // This is different from 'type' which denotes the Scheduled Job type
    enum class MonitorType(val value: String) {
        QUERY_LEVEL_MONITOR("query_level_monitor"),
        BUCKET_LEVEL_MONITOR("bucket_level_monitor"),
        CLUSTER_METRICS_MONITOR("cluster_metrics_monitor"),
        DOC_LEVEL_MONITOR("doc_level_monitor");

        override fun toString(): String {
            return value
        }
    }

    /** Returns a representation of the monitor suitable for passing into painless and mustache scripts. */
    fun asTemplateArg(): Map<String, Any> {
        return mapOf(_ID to id, _VERSION to version, NAME_FIELD to name, ENABLED_FIELD to enabled)
    }

    fun toXContentWithUser(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, params, false)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, params, true)
    }

    private fun createXContentBuilder(builder: XContentBuilder, params: ToXContent.Params, secure: Boolean): XContentBuilder {
        builder.startObject()
        if (params.paramAsBoolean("with_type", false)) builder.startObject(type)
        builder.field(TYPE_FIELD, type)
            .field(SCHEMA_VERSION_FIELD, schemaVersion)
            .field(NAME_FIELD, name)
            .field(MONITOR_TYPE_FIELD, monitorType)

        if (!secure) {
            builder.optionalUserField(USER_FIELD, user)
        }

        builder.field(ENABLED_FIELD, enabled)
            .optionalTimeField(ENABLED_TIME_FIELD, enabledTime)
            .field(SCHEDULE_FIELD, schedule)
            .field(INPUTS_FIELD, inputs.toTypedArray())
            .field(TRIGGERS_FIELD, triggers.toTypedArray())
            .optionalTimeField(LAST_UPDATE_TIME_FIELD, lastUpdateTime)
        if (uiMetadata.isNotEmpty()) builder.field(UI_METADATA_FIELD, uiMetadata)
        builder.field(DATA_SOURCES_FIELD, dataSources)
        builder.field(OWNER_FIELD, owner)
        if (params.paramAsBoolean("with_type", false)) builder.endObject()
        return builder.endObject()
    }

    override fun fromDocument(id: String, version: Long): Monitor = copy(id = id, version = version)

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
        schedule.writeTo(out)
        out.writeInstant(lastUpdateTime)
        out.writeOptionalInstant(enabledTime)
        out.writeString(monitorType)
        out.writeBoolean(user != null)
        user?.writeTo(out)
        out.writeInt(schemaVersion)
        // Outputting type with each Input so that the generic Input.readFrom() can read it
        out.writeVInt(inputs.size)
        inputs.forEach {
            if (it is SearchInput) {
                out.writeEnum(Input.Type.SEARCH_INPUT)
            } else {
                out.writeEnum(Input.Type.DOCUMENT_LEVEL_INPUT)
            }
            it.writeTo(out)
        }
        // Outputting type with each Trigger so that the generic Trigger.readFrom() can read it
        out.writeVInt(triggers.size)
        triggers.forEach {
            when (it) {
                is BucketLevelTrigger -> out.writeEnum(Trigger.Type.BUCKET_LEVEL_TRIGGER)
                is DocumentLevelTrigger -> out.writeEnum(Trigger.Type.DOCUMENT_LEVEL_TRIGGER)
                else -> out.writeEnum(Trigger.Type.QUERY_LEVEL_TRIGGER)
            }
            it.writeTo(out)
        }
        out.writeMap(uiMetadata)
        out.writeBoolean(dataSources != null) // for backward compatibility with pre-existing monitors which don't have datasources field
        dataSources.writeTo(out)
        out.writeOptionalString(owner)
    }

    companion object {
        const val MONITOR_TYPE = "monitor"
        const val TYPE_FIELD = "type"
        const val MONITOR_TYPE_FIELD = "monitor_type"
        const val SCHEMA_VERSION_FIELD = "schema_version"
        const val NAME_FIELD = "name"
        const val USER_FIELD = "user"
        const val ENABLED_FIELD = "enabled"
        const val SCHEDULE_FIELD = "schedule"
        const val TRIGGERS_FIELD = "triggers"
        const val NO_ID = ""
        const val NO_VERSION = 1L
        const val INPUTS_FIELD = "inputs"
        const val LAST_UPDATE_TIME_FIELD = "last_update_time"
        const val UI_METADATA_FIELD = "ui_metadata"
        const val DATA_SOURCES_FIELD = "data_sources"
        const val ENABLED_TIME_FIELD = "enabled_time"
        const val OWNER_FIELD = "owner"
        val MONITOR_TYPE_PATTERN = Pattern.compile("[a-zA-Z0-9_]{5,25}")

        // This is defined here instead of in ScheduledJob to avoid having the ScheduledJob class know about all
        // the different subclasses and creating circular dependencies
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            ScheduledJob::class.java,
            ParseField(MONITOR_TYPE),
            CheckedFunction { parse(it) }
        )

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): Monitor {
            var name: String? = null
            // Default to QUERY_LEVEL_MONITOR to cover Monitors that existed before the addition of MonitorType
            var monitorType: String = MonitorType.QUERY_LEVEL_MONITOR.toString()
            var user: User? = null
            var schedule: Schedule? = null
            var lastUpdateTime: Instant? = null
            var enabledTime: Instant? = null
            var uiMetadata: Map<String, Any> = mapOf()
            var enabled = true
            var schemaVersion = NO_SCHEMA_VERSION
            val triggers: MutableList<Trigger> = mutableListOf()
            val inputs: MutableList<Input> = mutableListOf()
            var dataSources = DataSources()
            var owner = "alerting"

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    SCHEMA_VERSION_FIELD -> schemaVersion = xcp.intValue()
                    NAME_FIELD -> name = xcp.text()
                    MONITOR_TYPE_FIELD -> {
                        monitorType = xcp.text()
                        val matcher = MONITOR_TYPE_PATTERN.matcher(monitorType)
                        val find = matcher.matches()
                        if (!find) {
                            throw IllegalStateException("Monitor type should follow pattern ${MONITOR_TYPE_PATTERN.pattern()}")
                        }
                    }
                    USER_FIELD -> user = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) null else User.parse(xcp)
                    ENABLED_FIELD -> enabled = xcp.booleanValue()
                    SCHEDULE_FIELD -> schedule = Schedule.parse(xcp)
                    INPUTS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val input = Input.parse(xcp)
                            if (input is ClusterMetricsInput) {
                                supportedClusterMetricsSettings?.validateApiType(input)
                            }
                            inputs.add(input)
                        }
                    }
                    TRIGGERS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            triggers.add(Trigger.parse(xcp))
                        }
                    }
                    ENABLED_TIME_FIELD -> enabledTime = xcp.instant()
                    LAST_UPDATE_TIME_FIELD -> lastUpdateTime = xcp.instant()
                    UI_METADATA_FIELD -> uiMetadata = xcp.map()
                    DATA_SOURCES_FIELD -> dataSources = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) {
                        DataSources()
                    } else {
                        DataSources.parse(xcp)
                    }
                    OWNER_FIELD -> owner = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) owner else xcp.text()
                    else -> {
                        xcp.skipChildren()
                    }
                }
            }

            if (enabled && enabledTime == null) {
                enabledTime = Instant.now()
            } else if (!enabled) {
                enabledTime = null
            }
            return Monitor(
                id,
                version,
                requireNotNull(name) { "Monitor name is null" },
                enabled,
                requireNotNull(schedule) { "Monitor schedule is null" },
                lastUpdateTime ?: Instant.now(),
                enabledTime,
                monitorType,
                user,
                schemaVersion,
                inputs.toList(),
                triggers.toList(),
                uiMetadata,
                dataSources,
                owner
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Monitor? {
            return Monitor(sin)
        }

        @Suppress("UNCHECKED_CAST")
        fun suppressWarning(map: MutableMap<String?, Any?>?): MutableMap<String, Any> {
            return map as MutableMap<String, Any>
        }
    }
}
