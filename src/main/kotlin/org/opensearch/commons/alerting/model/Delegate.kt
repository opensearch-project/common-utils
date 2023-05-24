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
 * Each underlying monitors defined in the composite monitor sequence input.
 * They are executed sequentially in the order mentioned.
 * Optionally accepts chained findings context.
 * */
data class Delegate(
    /**
     * Defines the order of the monitor in delegate list
     */
    val order: Int,
    /**
     * Id of the monitor
     */
    val monitorId: String,
    /**
     * Keeps the track of the previously executed monitor in a chain list.
     * Used for pre-filtering by getting the findings doc ids for the given monitor
     */
    val chainedMonitorFindings: ChainedMonitorFindings? = null
) : BaseModel {

    init {
        validateId(monitorId)
        validateOrder(order)
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        order = sin.readInt(),
        monitorId = sin.readString(),
        chainedMonitorFindings = if (sin.readBoolean()) {
            ChainedMonitorFindings(sin)
        } else null,
    )

    fun asTemplateArg(): Map<String, Any> {
        return mapOf(
            ORDER_FIELD to order,
            MONITOR_ID_FIELD to monitorId,
        )
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeInt(order)
        out.writeString(monitorId)
        out.writeBoolean(chainedMonitorFindings != null)
        chainedMonitorFindings?.writeTo(out)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(ORDER_FIELD, order)
            .field(MONITOR_ID_FIELD, monitorId)
        if (chainedMonitorFindings != null) {
            builder.field(CHAINED_FINDINGS_FIELD, chainedMonitorFindings)
        }
        builder.endObject()
        return builder
    }

    companion object {
        const val ORDER_FIELD = "order"
        const val MONITOR_ID_FIELD = "monitor_id"
        const val CHAINED_FINDINGS_FIELD = "chained_monitor_findings"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Delegate {
            lateinit var monitorId: String
            var order = 0
            var chainedMonitorFindings: ChainedMonitorFindings? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ORDER_FIELD -> {
                        order = xcp.intValue()
                        validateOrder(order)
                    }
                    MONITOR_ID_FIELD -> {
                        monitorId = xcp.text()
                        validateId(monitorId)
                    }
                    CHAINED_FINDINGS_FIELD -> {
                        chainedMonitorFindings = ChainedMonitorFindings.parse(xcp)
                    }
                }
            }
            return Delegate(order, monitorId, chainedMonitorFindings)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Delegate {
            return Delegate(sin)
        }

        fun validateOrder(order: Int) {
            require(order > 0) { "Invalid delgate order" }
        }
    }
}
