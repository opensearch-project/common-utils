package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

/** Delegate monitors passed as input for composite monitors. */
data class Sequence(
    val delegates: List<Delegate>
) : BaseModel {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readList(::Delegate)
    )

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            DELEGATES_FIELD to delegates,
        )
    }

    companion object {
        const val SEQUENCE_FIELD = "sequence"
        const val DELEGATES_FIELD = "delegates"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Sequence {
            val delegates: MutableList<Delegate> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    DELEGATES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            delegates.add(Delegate.parse(xcp))
                        }
                    }
                }
            }
            return Sequence(delegates)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): DocLevelMonitorInput {
            return DocLevelMonitorInput(sin)
        }
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeCollection(delegates)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(DELEGATES_FIELD, delegates.toTypedArray())
            .endObject()
    }
}
