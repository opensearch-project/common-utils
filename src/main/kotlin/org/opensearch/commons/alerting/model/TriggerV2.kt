package org.opensearch.commons.alerting.model

import java.io.IOException
import org.opensearch.commons.alerting.model.PPLTrigger.Companion.PPL_TRIGGER_FIELD
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

interface TriggerV2 : BaseModel {

    val id: String
    val name: String
    val severity: Severity
    // val expires // TODO: potentially need to use OScore's TimeValue
    val actions: List<Action>

    enum class TriggerV2Type(val value: String) {
        PPL_TRIGGER(PPL_TRIGGER_FIELD);

        override fun toString(): String {
            return value
        }
    }

    enum class Severity(val value: String) {
        INFO("info"),
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        CRITICAL("critical");

        companion object {
            fun enumFromString(value: String): Severity? {
                return entries.find { it.value == value }
            }
        }
    }

    companion object {
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val SEVERITY_FIELD = "severity"
        const val SUPPRESS_FIELD = "suppress"
        const val EXPIRES_FIELD = "expires"
        const val ACTIONS_FIELD = "actions"

//        @Throws(IOException::class)
//        fun parse(xcp: XContentParser): TriggerV2 {
//            // TODO: dead code until a MonitorV2 interface level parse() that delegates by monitor type is implemented
//            val trigger: TriggerV2
//
//            val triggerV2TypeNames = TriggerV2Type.entries.map { it.value }
//
//            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
//            XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, xcp.nextToken(), xcp)
//
//            if (!triggerV2TypeNames.contains(xcp.currentName())) {
//                throw IllegalArgumentException("Invalid trigger type ${xcp.currentName()}")
//            }
//
//            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
//            trigger = xcp.namedObject(TriggerV2::class.java, xcp.currentName(), null)
//            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, xcp.nextToken(), xcp)
//
//            return trigger
//        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): TriggerV2 {
            return when (val type = sin.readEnum(TriggerV2Type::class.java)) {
                TriggerV2Type.PPL_TRIGGER -> PPLTrigger(sin)
                else -> throw IllegalStateException("Unexpected input [$type] when reading TriggerV2")
            }
        }
    }
}