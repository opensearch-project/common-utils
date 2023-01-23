package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import java.io.IOException

/** Delegate monitors passed as input for composite monitors. */
data class Sequence(
    val delegates: List<Delegate>,
    val ruleIdMonitorIdMap: Map<String, String>? = null,
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
        const val RULE_ID_MONITOR_ID_FIELD = "rule_id_monitor_id_map"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Sequence {
            val delegates: MutableList<Delegate> = mutableListOf()
            var ruleIdMonitorIdMap: MutableMap<String, String> = mutableMapOf()

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
                    RULE_ID_MONITOR_ID_FIELD -> {
                        ruleIdMonitorIdMap = xcp.mapStrings()
                    }
                }
            }
            return Sequence(delegates, ruleIdMonitorIdMap)
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
            .field(RULE_ID_MONITOR_ID_FIELD, ruleIdMonitorIdMap)
            .endObject()
    }
}
