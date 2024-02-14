package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.common.UUIDs
import org.opensearch.commons.alerting.aggregation.bucketselectorext.BucketSelectorExtAggregationBuilder
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

data class BucketLevelTrigger(
    override val id: String = UUIDs.base64UUID(),
    override val name: String,
    override val severity: String,
    val bucketSelector: BucketSelectorExtAggregationBuilder,
    override val actions: List<Action>
) : Trigger {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readString(), // name
        sin.readString(), // severity
        BucketSelectorExtAggregationBuilder(sin), // condition
        sin.readList(::Action) // actions
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .startObject(BUCKET_LEVEL_TRIGGER_FIELD)
            .field(ID_FIELD, id)
            .field(NAME_FIELD, name)
            .field(SEVERITY_FIELD, severity)
            .startObject(CONDITION_FIELD)
        bucketSelector.internalXContent(builder, params)
        builder.endObject()
            .field(ACTIONS_FIELD, actions.toTypedArray())
            .endObject()
            .endObject()
        return builder
    }

    override fun name(): String {
        return BUCKET_LEVEL_TRIGGER_FIELD
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeString(severity)
        bucketSelector.writeTo(out)
        out.writeCollection(actions)
    }

    fun asTemplateArg(): Map<String, Any> {
        return mapOf(
            ID_FIELD to id,
            NAME_FIELD to name,
            SEVERITY_FIELD to severity,
            ACTIONS_FIELD to actions.map { it.asTemplateArg() },
            PARENT_BUCKET_PATH to getParentBucketPath(),
            CONDITION_FIELD to getCondition()
        )
    }

    fun getParentBucketPath(): String {
        return bucketSelector.parentBucketPath
    }

    fun getCondition(): String {
        return bucketSelector.script.idOrCode
    }

    companion object {
        const val BUCKET_LEVEL_TRIGGER_FIELD = "bucket_level_trigger"
        const val CONDITION_FIELD = "condition"
        const val PARENT_BUCKET_PATH = "parentBucketPath"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Trigger::class.java,
            ParseField(BUCKET_LEVEL_TRIGGER_FIELD),
            CheckedFunction { parseInner(it) }
        )

        @JvmStatic
        @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): BucketLevelTrigger {
            var id = UUIDs.base64UUID() // assign a default triggerId if one is not specified
            lateinit var name: String
            lateinit var severity: String
            val actions: MutableList<Action> = mutableListOf()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            lateinit var bucketSelector: BucketSelectorExtAggregationBuilder

            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()

                xcp.nextToken()
                when (fieldName) {
                    ID_FIELD -> id = xcp.text()
                    NAME_FIELD -> name = xcp.text()
                    SEVERITY_FIELD -> severity = xcp.text()
                    CONDITION_FIELD -> {
                        // Using the trigger id as the name in the bucket selector since it is validated for uniqueness within Monitors.
                        // The contents of the trigger definition are round-tripped through parse and toXContent during Monitor creation
                        // ensuring that the id is available here in the version of the Monitor object that will be executed, even if the
                        // user submitted a custom trigger id after the condition definition.
                        bucketSelector = BucketSelectorExtAggregationBuilder.parse(id, xcp)
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
                }
            }

            return BucketLevelTrigger(
                id = requireNotNull(id) { "Trigger id is null." },
                name = requireNotNull(name) { "Trigger name is null" },
                severity = requireNotNull(severity) { "Trigger severity is null" },
                bucketSelector = requireNotNull(bucketSelector) { "Trigger condition is null" },
                actions = requireNotNull(actions) { "Trigger actions are null" }
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): BucketLevelTrigger {
            return BucketLevelTrigger(sin)
        }
    }
}
