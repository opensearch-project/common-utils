/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.common.UUIDs
import org.opensearch.commons.alerting.model.Monitor.Companion.ALERTING_MAX_NAME_LENGTH
import org.opensearch.commons.alerting.model.Monitor.Companion.UUID_LENGTH
import org.opensearch.commons.alerting.model.Trigger.Companion.ACTIONS_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.ID_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.SEVERITY_FIELD
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

/**
 * The PPL Trigger for PPL Monitors
 *
 * There are two types of PPLTrigger conditions: NUMBER_OF_RESULTS and CUSTOM
 * NUMBER_OF_RESULTS: triggers based on whether the number of query results returned by the PPLMonitor
 *                    query meets some threshold
 * CUSTOM: triggers based on a custom condition that user specifies (a single ppl eval statement)
 *
 * @property id Trigger ID, defaults to a base64 UUID.
 * @property name Display name of the Trigger.
 * @property severity The severity level of the Trigger.
 * @property actions List of notification-sending actions to run when the Trigger condition is met.
 * @property conditionType The type of condition to evaluate.
 *               Can be either [ConditionType.NUMBER_OF_RESULTS] or [ConditionType.CUSTOM].
 * @property numResultsCondition The comparison operator for NUMBER_OF_RESULTS conditions. Required if using NUMBER_OF_RESULTS conditions,
 *                               required to be null otherwise.
 * @property numResultsValue The threshold value for NUMBER_OF_RESULTS conditions. Required if using NUMBER_OF_RESULTS conditions,
 *                           required to be null otherwise.
 * @property customCondition A custom condition expression. Required if using CUSTOM conditions,
 *                           required to be null otherwise.
 *
 * @opensearch.experimental
 */
data class PPLTrigger(
    override val id: String = UUIDs.base64UUID(),
    override val name: String,
    override val severity: String,
    override val actions: List<Action>,
    val conditionType: ConditionType, // NUMBER_OF_RESULTS or CUSTOM
    val numResultsCondition: NumResultsCondition?,
    val numResultsValue: Long?,
    val customCondition: String?
) : Trigger {

    init {
        requireNotNull(this.name) { "Trigger name must be included." }
        requireNotNull(this.severity) { "Trigger severity must be included." }
        requireNotNull(this.conditionType) { "Trigger condition type must be included." }

        require(this.id.length <= UUID_LENGTH) {
            "Trigger ID too long, length must be less than $UUID_LENGTH."
        }

        require(this.name.length <= ALERTING_MAX_NAME_LENGTH) {
            "Trigger name too long, length must be less than $ALERTING_MAX_NAME_LENGTH."
        }

        this.actions.forEach {
            require(it.name.length <= ALERTING_MAX_NAME_LENGTH) {
                "Name of action with ID ${it.id} too long, length must be less than $ALERTING_MAX_NAME_LENGTH."
            }
            require(it.destinationId.length <= NOTIFICATIONS_ID_MAX_LENGTH) {
                "Channel ID of action with ID ${it.id} too long, length must be less than $NOTIFICATIONS_ID_MAX_LENGTH."
            }
            require(it.destinationId.isNotEmpty()) {
                "Channel ID should not be empty."
            }
            require(it.destinationId.matches(validCharsRegex)) {
                "Channel ID should only have alphanumeric characters, dashes, and underscores."
            }
        }

        when (this.conditionType) {
            ConditionType.NUMBER_OF_RESULTS -> {
                requireNotNull(this.numResultsCondition) {
                    "if trigger condition is of type ${ConditionType.NUMBER_OF_RESULTS.value}, " +
                        "$NUM_RESULTS_CONDITION_FIELD must be included."
                }
                requireNotNull(this.numResultsValue) {
                    "if trigger condition is of type ${ConditionType.NUMBER_OF_RESULTS.value}, " +
                        "$NUM_RESULTS_VALUE_FIELD must be included."
                }
                require(this.customCondition == null) {
                    "if trigger condition is of type ${ConditionType.NUMBER_OF_RESULTS.value}, " +
                        "$CUSTOM_CONDITION_FIELD must not be included."
                }
            }
            ConditionType.CUSTOM -> {
                requireNotNull(this.customCondition) {
                    "if trigger condition is of type ${ConditionType.CUSTOM.value}, " +
                        "$CUSTOM_CONDITION_FIELD must be included."
                }
                require(this.numResultsCondition == null) {
                    "if trigger condition is of type ${ConditionType.CUSTOM.value}, " +
                        "$NUM_RESULTS_CONDITION_FIELD must not be included."
                }
                require(this.numResultsValue == null) {
                    "if trigger condition is of type ${ConditionType.CUSTOM.value}, " +
                        "$NUM_RESULTS_VALUE_FIELD must not be included."
                }
            }
        }

        if (conditionType == ConditionType.NUMBER_OF_RESULTS) {
            require(this.numResultsValue!! >= 0L) { "Number of results to check for cannot be negative." }
        }
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readString(), // name
        sin.readString(), // severity
        sin.readList(::Action), // actions
        sin.readEnum(ConditionType::class.java), // conditionType
        if (sin.readBoolean()) sin.readEnum(NumResultsCondition::class.java) else null, // numResultsCondition
        sin.readOptionalLong(), // numResultsValue
        sin.readOptionalString() // customCondition
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeString(severity)
        out.writeCollection(actions)
        out.writeEnum(conditionType)

        out.writeBoolean(numResultsCondition != null)
        numResultsCondition?.let { out.writeEnum(numResultsCondition) }

        out.writeOptionalLong(numResultsValue)
        out.writeOptionalString(customCondition)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params?): XContentBuilder {
        builder.startObject()
        builder.startObject(PPL_TRIGGER_FIELD)
        builder.field(ID_FIELD, id)
        builder.field(NAME_FIELD, name)
        builder.field(SEVERITY_FIELD, severity)
        builder.field(ACTIONS_FIELD, actions.toTypedArray())
        builder.field(CONDITION_TYPE_FIELD, conditionType.value)
        numResultsCondition?.let { builder.field(NUM_RESULTS_CONDITION_FIELD, numResultsCondition.value) }
        numResultsValue?.let { builder.field(NUM_RESULTS_VALUE_FIELD, numResultsValue) }
        customCondition?.let { builder.field(CUSTOM_CONDITION_FIELD, customCondition) }
        builder.endObject()
        builder.endObject()
        return builder
    }

    override fun asTemplateArg(): Map<String, Any> {
        val templateArg = mutableMapOf(
            ID_FIELD to id,
            NAME_FIELD to name,
            SEVERITY_FIELD to severity,
            ACTIONS_FIELD to actions.map { it.asTemplateArg() },
            CONDITION_TYPE_FIELD to conditionType.value
        )

        if (conditionType == ConditionType.NUMBER_OF_RESULTS) {
            templateArg[NUM_RESULTS_CONDITION_FIELD] = numResultsCondition!!.value
            templateArg[NUM_RESULTS_VALUE_FIELD] = numResultsValue!!
        } else {
            templateArg[CUSTOM_CONDITION_FIELD] = customCondition!!
        }

        return templateArg
    }

    override fun name(): String {
        return PPL_TRIGGER_FIELD
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
        // trigger wrapper object field name
        const val PPL_TRIGGER_FIELD = "ppl_trigger"

        // field names
        const val CONDITION_TYPE_FIELD = "type"
        const val NUM_RESULTS_CONDITION_FIELD = "num_results_condition"
        const val NUM_RESULTS_VALUE_FIELD = "num_results_value"
        const val CUSTOM_CONDITION_FIELD = "custom_condition"

        // hard, nonadjustable limits
        const val NOTIFICATIONS_ID_MAX_LENGTH = 512 // length limit for notifications channel custom ID at channel creation time

        // regular expression for validating that a string contains
        // only valid chars (letters, numbers, -, _)
        private val validCharsRegex = """^[a-zA-Z0-9_-]+$""".toRegex()

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Trigger::class.java,
            ParseField(PPL_TRIGGER_FIELD),
            CheckedFunction { parseInner(it) }
        )

        @JvmStatic
        @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): PPLTrigger {
            var id = UUIDs.base64UUID() // assign a default triggerId if one is not specified
            var name: String? = null
            var severity: String? = null
            val actions: MutableList<Action> = mutableListOf()
            var conditionType: ConditionType? = null
            var numResultsCondition: NumResultsCondition? = null
            var numResultsValue: Long? = null
            var customCondition: String? = null

            /* parse */
            if (xcp.currentToken() != XContentParser.Token.START_OBJECT && xcp.currentToken() != XContentParser.Token.FIELD_NAME) {
                XContentParserUtils.throwUnknownToken(xcp.currentToken(), xcp.tokenLocation)
            }

            // If the parser began on START_OBJECT, move to the next token so that the while loop enters on
            // the fieldName (or END_OBJECT if it's empty).
            if (xcp.currentToken() == XContentParser.Token.START_OBJECT) xcp.nextToken()

            while (xcp.currentToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ID_FIELD -> id = xcp.text()
                    NAME_FIELD -> name = xcp.text()
                    SEVERITY_FIELD -> severity = xcp.text()
                    CONDITION_TYPE_FIELD -> {
                        val input = xcp.text()
                        val enumMatchResult = ConditionType.enumFromString(input)
                            ?: throw IllegalArgumentException(
                                "Invalid value for $CONDITION_TYPE_FIELD: $input. " +
                                    "Supported values are ${ConditionType.entries.map { it.value }}"
                            )
                        conditionType = enumMatchResult
                    }
                    NUM_RESULTS_CONDITION_FIELD -> {
                        if (xcp.currentToken() != XContentParser.Token.VALUE_NULL) {
                            val input = xcp.text()
                            val enumMatchResult = NumResultsCondition.enumFromString(input)
                                ?: throw IllegalArgumentException(
                                    "Invalid value for $NUM_RESULTS_CONDITION_FIELD: $input. " +
                                        "Supported values are ${NumResultsCondition.entries.map { it.value }}"
                                )
                            numResultsCondition = enumMatchResult
                        }
                    }
                    NUM_RESULTS_VALUE_FIELD -> {
                        if (xcp.currentToken() != XContentParser.Token.VALUE_NULL) {
                            numResultsValue = xcp.longValue()
                        }
                    }
                    CUSTOM_CONDITION_FIELD -> {
                        if (xcp.currentToken() != XContentParser.Token.VALUE_NULL) {
                            customCondition = xcp.text()
                        }
                    }
                    ACTIONS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actions.add(Action.parse(xcp))
                        }
                    }
                    else -> throw IllegalArgumentException("Unexpected field when parsing PPL Trigger: $fieldName")
                }

                xcp.nextToken()
            }

            /* validations */
            requireNotNull(name) { "Trigger name must be included" }
            requireNotNull(severity) { "Trigger severity must be included" }
            requireNotNull(conditionType) { "Trigger condition type must be included" }

            // 3. prepare and return PPLTrigger object
            return PPLTrigger(
                id,
                name,
                severity,
                actions,
                conditionType,
                numResultsCondition,
                numResultsValue,
                customCondition
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): PPLTrigger {
            return PPLTrigger(sin)
        }
    }
}
