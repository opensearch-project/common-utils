package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.common.UUIDs
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.core.ParseField
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

data class NoOpTrigger(
    override val id: String = UUIDs.base64UUID(),
    override val name: String = "NoOp trigger",
    override val severity: String = "",
    override val actions: List<Action> = listOf(),
) : Trigger {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this()

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .startObject(NOOP_TRIGGER_FIELD)
            .endObject()
        return builder
    }

    override fun name(): String {
        return NOOP_TRIGGER_FIELD
    }

    fun asTemplateArg(): Map<String, Any> {
        return mapOf()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
    }

    companion object {
        const val NOOP_TRIGGER_FIELD = "noop_trigger"
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Trigger::class.java, ParseField(NOOP_TRIGGER_FIELD),
            CheckedFunction { parseInner(it) }
        )

        @JvmStatic @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): NoOpTrigger {
            if (xcp.currentToken() != XContentParser.Token.START_OBJECT && xcp.currentToken() != XContentParser.Token.FIELD_NAME) {
                XContentParserUtils.throwUnknownToken(xcp.currentToken(), xcp.tokenLocation)
            }

            // If the parser began on START_OBJECT, move to the next token so that the while loop enters on
            // the fieldName (or END_OBJECT if it's empty).
            if (xcp.currentToken() == XContentParser.Token.START_OBJECT) xcp.nextToken()
            if (xcp.currentToken() != XContentParser.Token.END_OBJECT) {
                XContentParserUtils.throwUnknownToken(xcp.currentToken(), xcp.tokenLocation)
            } else {
                xcp.nextToken()
            }
            return NoOpTrigger()
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): NoOpTrigger {
            return NoOpTrigger(sin)
        }
    }
}
