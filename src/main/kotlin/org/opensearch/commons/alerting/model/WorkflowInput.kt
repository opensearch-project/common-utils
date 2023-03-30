package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

interface WorkflowInput : BaseModel {

    enum class Type(val value: String) {
        COMPOSITE_INPUT(CompositeInput.COMPOSITE_INPUT_FIELD);

        override fun toString(): String {
            return value
        }
    }

    companion object {

        @Throws(IOException::class)
        fun parse(xcp: XContentParser): WorkflowInput {
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, xcp.nextToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            val input = if (xcp.currentName() == Type.COMPOSITE_INPUT.value) {
                CompositeInput.parse(xcp)
            } else {
                throw IllegalStateException("Unexpected input type when reading Input")
            }
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, xcp.nextToken(), xcp)
            return input
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): WorkflowInput {
            return when (val type = sin.readEnum(Type::class.java)) {
                Type.COMPOSITE_INPUT -> CompositeInput(sin)
                // This shouldn't be reachable but ensuring exhaustiveness as Kotlin warns
                // enum can be null in Java
                else -> throw IllegalStateException("Unexpected input [$type] when reading Trigger")
            }
        }
    }

    fun name(): String
}
