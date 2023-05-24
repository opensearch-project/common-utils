package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.commons.utils.validateId
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

/**
 * Context passed in delegate monitor to filter data queried by a monitor based on the findings of the given monitor id.
 */
// TODO - Remove the class and move the monitorId to Delegate (as a chainedMonitorId property) if this class won't be updated by adding new properties
data class ChainedMonitorFindings(
    val monitorId: String
) : BaseModel {

    init {
        validateId(monitorId)
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // monitorId
    )

    fun asTemplateArg(): Map<String, Any> {
        return mapOf(
            MONITOR_ID_FIELD to monitorId,
        )
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(MONITOR_ID_FIELD, monitorId)
            .endObject()
        return builder
    }

    companion object {
        const val MONITOR_ID_FIELD = "monitor_id"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): ChainedMonitorFindings {
            lateinit var monitorId: String

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    MONITOR_ID_FIELD -> {
                        monitorId = xcp.text()
                        validateId(monitorId)
                    }
                }
            }
            return ChainedMonitorFindings(monitorId)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): ChainedMonitorFindings {
            return ChainedMonitorFindings(sin)
        }
    }
}
