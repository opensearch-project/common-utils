package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.alerting.util.IndexUtils.Companion.NO_SCHEMA_VERSION
import org.opensearch.commons.alerting.util.IndexUtils.Companion.WORKFLOW_MAX_INPUTS
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.alerting.util.instant
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.commons.alerting.util.optionalUserField
import org.opensearch.commons.authuser.User
import org.opensearch.core.ParseField
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException
import java.time.Instant
import java.util.Locale

data class Workflow(
    override val id: String = NO_ID,
    override val version: Long = NO_VERSION,
    override val name: String,
    override val enabled: Boolean,
    override val schedule: Schedule,
    override val lastUpdateTime: Instant,
    override val enabledTime: Instant?,
    // TODO: Check how this behaves during rolling upgrade/multi-version cluster
    //  Can read/write and parsing break if it's done from an old -> new version of the plugin?
    val workflowType: WorkflowType,
    val user: User?,
    val schemaVersion: Int = NO_SCHEMA_VERSION,
    val inputs: List<WorkflowInput>,
    val owner: String? = DEFAULT_OWNER
) : ScheduledJob {
    override val type = WORKFLOW_TYPE

    init {
        if (enabled) {
            requireNotNull(enabledTime)
        } else {
            require(enabledTime == null)
        }
        require(inputs.size <= WORKFLOW_MAX_INPUTS) { "Workflows can only have $WORKFLOW_MAX_INPUTS search input." }
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
        workflowType = sin.readEnum(WorkflowType::class.java),
        user = if (sin.readBoolean()) {
            User(sin)
        } else null,
        schemaVersion = sin.readInt(),
        inputs = sin.readList((WorkflowInput)::readFrom),
        owner = sin.readOptionalString()
    )

    // This enum classifies different workflows
    // This is different from 'type' which denotes the Scheduled Job type
    enum class WorkflowType(val value: String) {
        COMPOSITE("composite");

        override fun toString(): String {
            return value
        }
    }

    /** Returns a representation of the workflow suitable for passing into painless and mustache scripts. */
    fun asTemplateArg(): Map<String, Any> {
        return mapOf(_ID to id, _VERSION to version, NAME_FIELD to name, ENABLED_FIELD to enabled)
    }

    fun toXContentWithUser(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, params, false)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return createXContentBuilder(builder, params, true)
    }

    private fun createXContentBuilder(
        builder: XContentBuilder,
        params: ToXContent.Params,
        secure: Boolean
    ): XContentBuilder {
        builder.startObject()
        if (params.paramAsBoolean("with_type", false)) builder.startObject(type)
        builder.field(TYPE_FIELD, type)
            .field(SCHEMA_VERSION_FIELD, schemaVersion)
            .field(NAME_FIELD, name)
            .field(WORKFLOW_TYPE_FIELD, workflowType)

        if (!secure) {
            builder.optionalUserField(USER_FIELD, user)
        }

        builder.field(ENABLED_FIELD, enabled)
            .optionalTimeField(ENABLED_TIME_FIELD, enabledTime)
            .field(SCHEDULE_FIELD, schedule)
            .field(INPUTS_FIELD, inputs.toTypedArray())
            .optionalTimeField(LAST_UPDATE_TIME_FIELD, lastUpdateTime)
        builder.field(OWNER_FIELD, owner)
        if (params.paramAsBoolean("with_type", false)) builder.endObject()
        return builder.endObject()
    }

    override fun fromDocument(id: String, version: Long): Workflow = copy(id = id, version = version)

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
        out.writeEnum(workflowType)
        out.writeBoolean(user != null)
        user?.writeTo(out)
        out.writeInt(schemaVersion)
        // Outputting type with each Input so that the generic Input.readFrom() can read it
        out.writeVInt(inputs.size)
        inputs.forEach {
            if (it is CompositeInput) out.writeEnum(WorkflowInput.Type.COMPOSITE_INPUT)
            it.writeTo(out)
        }
        // Outputting type with each Trigger so that the generic Trigger.readFrom() can read it
        out.writeOptionalString(owner)
    }

    companion object {
        const val WORKFLOW_DELEGATE_PATH = "workflow.inputs.composite_input.sequence.delegates"
        const val WORKFLOW_MONITOR_PATH = "workflow.inputs.composite_input.sequence.delegates.monitor_id"
        const val WORKFLOW_TYPE = "workflow"
        const val TYPE_FIELD = "type"
        const val WORKFLOW_TYPE_FIELD = "workflow_type"
        const val SCHEMA_VERSION_FIELD = "schema_version"
        const val NAME_FIELD = "name"
        const val USER_FIELD = "user"
        const val ENABLED_FIELD = "enabled"
        const val SCHEDULE_FIELD = "schedule"
        const val NO_ID = ""
        const val NO_VERSION = 1L
        const val INPUTS_FIELD = "inputs"
        const val LAST_UPDATE_TIME_FIELD = "last_update_time"
        const val ENABLED_TIME_FIELD = "enabled_time"
        const val OWNER_FIELD = "owner"

        // This is defined here instead of in ScheduledJob to avoid having the ScheduledJob class know about all
        // the different subclasses and creating circular dependencies
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            ScheduledJob::class.java,
            ParseField(WORKFLOW_TYPE),
            CheckedFunction { parse(it) }
        )

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): Workflow {
            var name: String? = null
            var workflowType: String = WorkflowType.COMPOSITE.toString()
            var user: User? = null
            var schedule: Schedule? = null
            var lastUpdateTime: Instant? = null
            var enabledTime: Instant? = null
            var enabled = true
            var schemaVersion = NO_SCHEMA_VERSION
            val inputs: MutableList<WorkflowInput> = mutableListOf()
            var owner = DEFAULT_OWNER

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    SCHEMA_VERSION_FIELD -> schemaVersion = xcp.intValue()
                    NAME_FIELD -> name = xcp.text()
                    WORKFLOW_TYPE_FIELD -> {
                        workflowType = xcp.text()
                        val allowedTypes = WorkflowType.values().map { it.value }
                        if (!allowedTypes.contains(workflowType)) {
                            throw IllegalStateException("Workflow type should be one of $allowedTypes")
                        }
                    }
                    USER_FIELD -> {
                        user = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) null else User.parse(xcp)
                    }
                    ENABLED_FIELD -> enabled = xcp.booleanValue()
                    SCHEDULE_FIELD -> schedule = Schedule.parse(xcp)
                    INPUTS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val input = WorkflowInput.parse(xcp)
                            inputs.add(input)
                        }
                    }
                    ENABLED_TIME_FIELD -> enabledTime = xcp.instant()
                    LAST_UPDATE_TIME_FIELD -> lastUpdateTime = xcp.instant()
                    OWNER_FIELD -> {
                        owner = if (xcp.currentToken() == XContentParser.Token.VALUE_NULL) owner else xcp.text()
                    }
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
            return Workflow(
                id,
                version,
                requireNotNull(name) { "Workflow name is null" },
                enabled,
                requireNotNull(schedule) { "Workflow schedule is null" },
                lastUpdateTime ?: Instant.now(),
                enabledTime,
                WorkflowType.valueOf(workflowType.uppercase(Locale.ROOT)),
                user,
                schemaVersion,
                inputs.toList(),
                owner
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Workflow? {
            return Workflow(sin)
        }

        @Suppress("UNCHECKED_CAST")
        fun suppressWarning(map: MutableMap<String?, Any?>?): MutableMap<String, Any> {
            return map as MutableMap<String, Any>
        }

        private const val DEFAULT_OWNER = "alerting"
    }
}
