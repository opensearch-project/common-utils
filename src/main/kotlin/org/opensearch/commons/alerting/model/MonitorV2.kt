package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.opensearch.common.CheckedFunction
import org.opensearch.commons.alerting.model.Monitor.Companion
import org.opensearch.commons.alerting.model.Monitor.Companion.INPUTS_FIELD
import org.opensearch.commons.alerting.model.PPLMonitor.Companion.PPL_MONITOR_TYPE
import org.opensearch.commons.alerting.model.Trigger.Type
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._VERSION
import org.opensearch.commons.alerting.util.nonOptionalTimeField
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

// TODO: maybe make this abstract class? put init block logic here for all monitors?
interface MonitorV2 : ScheduledJob {
    override val id: String
    override val version: Long
    override val name: String
    override val enabled: Boolean
    override val schedule: Schedule
    override val lastUpdateTime: Instant // required for scheduled job maintenance
    override val enabledTime: Instant? // required for scheduled job maintenance
    val labels: Map<String, String>?
    val triggers: List<TriggerV2>

    enum class MonitorV2Type(val value: String) {
        PPL_MONITOR(PPL_MONITOR_TYPE);

        override fun toString(): String {
            return value
        }

        companion object {
            fun enumFromString(value: String): MonitorV2Type? {
                return MonitorV2Type.entries.find { it.value == value }
            }
        }
    }

    fun asTemplateArg(): Map<String, Any?>

    companion object {
        // scheduled job field names
        const val TYPE_FIELD = "type"
        const val MONITOR_V2_TYPE = "monitor_v2" // scheduled job type is MonitorV2

        // field names
        const val NAME_FIELD = "name"
        const val MONITOR_TYPE_FIELD = "monitor_type"
        const val ENABLED_FIELD = "enabled"
        const val SCHEDULE_FIELD = "schedule"
        const val LAST_UPDATE_TIME_FIELD = "last_update_time"
        const val ENABLED_TIME_FIELD = "enabled_time"
        const val LABELS_FIELD = "labels"
        const val TRIGGERS_FIELD = "triggers"

        // default values
        const val NO_ID = ""
        const val NO_VERSION = 1L

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            ScheduledJob::class.java,
            ParseField(MONITOR_V2_TYPE),
            CheckedFunction { parse(it) }
        )

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): MonitorV2 {
            /*
             TODO: this default implementation is short-term and inextensible
             a correct implementation should 1) scan for monitor type field
             2) delegate to the parse function of the MonitorV2 implementation,
             just like how TriggerV2 interface does it.
             The problem is the (internal) monitor type field is at the same
             level as all the other monitor fields, which means we would need some
             way of parsing the same XContent twice
             possible work around: require monitor type to be very first field
             if first monitor type field is absent, assume ppl monitor as default
             */
            return PPLMonitor.parse(xcp)
        }

        fun readFrom(sin: StreamInput): MonitorV2 {
            val monitorType = sin.readEnum(MonitorV2Type::class.java)
            return when (monitorType) {
                MonitorV2Type.PPL_MONITOR -> PPLMonitor(sin)
                else -> throw IllegalStateException("Unexpected input [$monitorType] when reading MonitorV2")
            }
        }

        fun writeTo(out: StreamOutput, monitorV2: MonitorV2) {
            when (monitorV2) {
                is PPLMonitor -> {
                    out.writeEnum(MonitorV2.MonitorV2Type.PPL_MONITOR)
                    monitorV2.writeTo(out)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun convertLabelsMap(map: Map<String, Any>): Map<String, String> {
            if (map.values.all { it is String }) {
                return map as Map<String, String>
            } else {
                throw ClassCastException("at least one value in the map was not a string: $map")
            }
        }
    }
}