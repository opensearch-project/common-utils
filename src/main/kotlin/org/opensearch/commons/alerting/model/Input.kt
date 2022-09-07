package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.alerting.model.ClusterMetricsInput.Companion.URI_FIELD
import org.opensearch.commons.alerting.model.DocLevelMonitorInput.Companion.DOC_LEVEL_INPUT_FIELD
import org.opensearch.commons.alerting.model.SearchInput.Companion.SEARCH_FIELD
import org.opensearch.commons.notifications.model.BaseModel
import java.io.IOException

interface Input : BaseModel {

    enum class Type(val value: String) {
        DOCUMENT_LEVEL_INPUT(DOC_LEVEL_INPUT_FIELD),
        CLUSTER_METRICS_INPUT(URI_FIELD),
        SEARCH_INPUT(SEARCH_FIELD);

        override fun toString(): String {
            return value
        }
    }

    companion object {

        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Input {
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, xcp.nextToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            val input = if (xcp.currentName() == Type.SEARCH_INPUT.value) {
                SearchInput.parseInner(xcp)
            } else if (xcp.currentName() == Type.CLUSTER_METRICS_INPUT.value) {
                ClusterMetricsInput.parseInner(xcp)
            } else {
                DocLevelMonitorInput.parse(xcp)
            }
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, xcp.nextToken(), xcp)
            return input
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Input {
            return when (val type = sin.readEnum(Input.Type::class.java)) {
                Type.DOCUMENT_LEVEL_INPUT -> DocLevelMonitorInput(sin)
                Type.CLUSTER_METRICS_INPUT -> ClusterMetricsInput(sin)
                Type.SEARCH_INPUT -> SearchInput(sin)
                // This shouldn't be reachable but ensuring exhaustiveness as Kotlin warns
                // enum can be null in Java
                else -> throw IllegalStateException("Unexpected input [$type] when reading Trigger")
            }
        }
    }

    fun name(): String
}
