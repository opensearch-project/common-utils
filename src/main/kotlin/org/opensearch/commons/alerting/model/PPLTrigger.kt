package org.opensearch.commons.alerting.model

import java.io.IOException
import org.opensearch.common.CheckedFunction
import org.opensearch.common.UUIDs
import org.opensearch.commons.alerting.model.TriggerV2.Companion.ACTIONS_FIELD
import org.opensearch.commons.alerting.model.TriggerV2.Companion.ID_FIELD
import org.opensearch.commons.alerting.model.TriggerV2.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.TriggerV2.Companion.SEVERITY_FIELD
import org.opensearch.commons.alerting.model.TriggerV2.Severity
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

data class PPLTrigger(
    override val id: String = UUIDs.base64UUID(),
    override val name: String,
    override val severity: Severity,
    override val actions: List<Action>,
    val mode: TriggerMode, // result_set or per_result
    // val suppress // TODO: potentially need to use OScore's TimeValue
    val conditionType: ConditionType,
    val numResultsCondition: NumResultsCondition?,
    val numResultsValue: Long?,
    val customCondition: String?
) : TriggerV2 {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readString(), // name
        sin.readEnum(Severity::class.java), // severity
        sin.readList(::Action), // actions
        sin.readEnum(TriggerMode::class.java), // trigger mode
        // TODO: add validation to ensure numResultsCondition and numResultsValue or customCondition are non-null based on condition type?
        sin.readEnum(ConditionType::class.java), // condition type
        // TODO: can updated StreamInput be picked up so we can use readOptionalEnum?
        if (sin.readBoolean()) sin.readEnum(NumResultsCondition::class.java) else null, // num results condition
        sin.readOptionalLong(), // num results value
        sin.readOptionalString(), // custom condition
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeEnum(severity)
        out.writeCollection(actions)
        out.writeEnum(mode)
        out.writeEnum(conditionType)
        out.writeBoolean(numResultsCondition != null) // TODO: look for built-in writeOptionalEnum support
        if (numResultsCondition != null) out.writeEnum(numResultsCondition)
        out.writeOptionalLong(numResultsValue)
        out.writeOptionalString(customCondition)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params?): XContentBuilder {
        builder.startObject()
        builder.startObject(PPL_TRIGGER_FIELD)
        builder.field(ID_FIELD, id)
        builder.field(NAME_FIELD, name)
        builder.field(MODE_FIELD, mode.value)
        builder.field(CONDITION_TYPE_FIELD, conditionType.value)
        builder.field(NUM_RESULTS_CONDITION_FIELD, numResultsCondition?.value)
        builder.field(NUM_RESULTS_VALUE_FIELD, numResultsValue)
        builder.field(CUSTOM_CONDITION_FIELD, customCondition)
        builder.field(SEVERITY_FIELD, severity.value)
        builder.field(ACTIONS_FIELD, actions.toTypedArray())
        builder.endObject()
        builder.endObject()
        return builder
    }

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            ID_FIELD to id,
            NAME_FIELD to name,
            MODE_FIELD to mode.value,
            CONDITION_TYPE_FIELD to conditionType.value,
            NUM_RESULTS_CONDITION_FIELD to numResultsCondition?.value,
            NUM_RESULTS_VALUE_FIELD to numResultsValue,
            CUSTOM_CONDITION_FIELD to customCondition,
            SEVERITY_FIELD to severity.value,
            ACTIONS_FIELD to actions.map { it.asTemplateArg() }
        )
    }

    enum class TriggerMode(val value: String) {
        RESULT_SET("result_set"),
        PER_RESULT("per_result");

        companion object {
            fun enumFromString(value: String): TriggerMode? = entries.firstOrNull { it.value == value }
        }
    }

    enum class ConditionType(val value: String) {
        NUMBER_OF_RESULTS("number_of_results"),
        CUSTOM("custom");

        companion object {
            fun enumFromString(value: String): ConditionType? = entries.firstOrNull { it.value == value }
        }
    }

    enum class NumResultsCondition(val value: String) {
        GREATER_THAN(">"),
        GREATER_THAN_EQUAL(">="),
        LESS_THAN("<"),
        LESS_THAN_EQUAL("<="),
        EQUAL("=="),
        NOT_EQUAL("!=");

        companion object {
            fun enumFromString(value: String): NumResultsCondition? = entries.firstOrNull { it.value == value }
        }
    }

    companion object {
        const val PPL_TRIGGER_FIELD = "ppl_trigger"

        const val MODE_FIELD = "mode"
        const val CONDITION_TYPE_FIELD = "type"
        const val NUM_RESULTS_CONDITION_FIELD = "num_results_condition"
        const val NUM_RESULTS_VALUE_FIELD = "num_results_value"
        const val CUSTOM_CONDITION_FIELD = "custom_condition"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            TriggerV2::class.java,
            ParseField(PPL_TRIGGER_FIELD),
            CheckedFunction { parseInner(it) }
        )

        @JvmStatic
        @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): PPLTrigger {
            var id = UUIDs.base64UUID() // assign a default triggerId if one is not specified
            var name: String? = null
            var severity: Severity? = null
            var mode: TriggerMode? = null
            var conditionType: ConditionType? = null
            var numResultsCondition: NumResultsCondition? = null
            var numResultsValue: Long? = null
            var customCondition: String? = null
            val actions: MutableList<Action> = mutableListOf()

            /* parse */
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)

            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ID_FIELD -> id = xcp.text()
                    NAME_FIELD -> name = xcp.text()
                    SEVERITY_FIELD -> {
                        val enumMatchResult = Severity.enumFromString(xcp.text())
                            ?: throw IllegalArgumentException("Invalid value for $SEVERITY_FIELD. Supported values are ${Severity.entries.map { it.value }}")
                        severity = enumMatchResult
                    }
                    MODE_FIELD -> {
                        val enumMatchResult = TriggerMode.enumFromString(xcp.text())
                            ?: throw IllegalArgumentException("Invalid value for $MODE_FIELD. Supported values are ${TriggerMode.entries.map { it.value }}")
                        mode = enumMatchResult
                    }
                    CONDITION_TYPE_FIELD -> {
                        val enumMatchResult = ConditionType.enumFromString(xcp.text())
                            ?: throw IllegalArgumentException("Invalid value for $CONDITION_TYPE_FIELD. Supported values are ${ConditionType.entries.map { it.value }}")
                        conditionType = enumMatchResult
                    }
                    NUM_RESULTS_CONDITION_FIELD -> numResultsCondition = NumResultsCondition.enumFromString(xcp.text())
                    NUM_RESULTS_VALUE_FIELD -> numResultsValue = xcp.longValue()
                    CUSTOM_CONDITION_FIELD -> customCondition = xcp.text()
                    ACTIONS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actions.add(Action.parse(xcp))
                        }
                    }
                }
            }

            /* validations */
            requireNotNull(name) { "Trigger name is null" }
            requireNotNull(severity) { "Severity is null" }
            requireNotNull(mode) { "Trigger mode is null" }
            requireNotNull(conditionType) { "Trigger condition type is null" }

            when (conditionType) {
                ConditionType.NUMBER_OF_RESULTS -> {
                    requireNotNull(numResultsCondition) { "if trigger condition is of type ${ConditionType.NUMBER_OF_RESULTS.value}, $NUM_RESULTS_CONDITION_FIELD cannot be null" }
                    requireNotNull(numResultsValue) { "if trigger condition is of type ${ConditionType.NUMBER_OF_RESULTS.value}, $NUM_RESULTS_VALUE_FIELD cannot be null" }
                }
                ConditionType.CUSTOM -> {
                    requireNotNull(customCondition) { "if trigger condition is of type ${ConditionType.CUSTOM.value}, $CUSTOM_CONDITION_FIELD cannot be null" }
                }
            }

            // 3. prepare and return PPLTrigger object
            return PPLTrigger(
                id,
                name,
                severity,
                actions,
                mode,
                conditionType,
                numResultsCondition,
                numResultsValue,
                customCondition,
            )
        }
    }
}
