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
    val order: Int,
    val monitorId: String,
    val chainedFindings: ChainedFindings? = null
) : BaseModel {

    init {
        validateId(monitorId)
        validateOrder(order)
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        order = sin.readInt(),
        monitorId = sin.readString(),
        chainedFindings = if (sin.readBoolean()) {
            ChainedFindings(sin)
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
        out.writeBoolean(chainedFindings != null)
        chainedFindings?.writeTo(out)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(ORDER_FIELD, order)
            .field(MONITOR_ID_FIELD, monitorId)
        if (chainedFindings != null) {
            builder.field(CHAINED_FINDINGS_FIELD, chainedFindings)
        }
        builder.endObject()
        return builder
    }

    companion object {
        const val ORDER_FIELD = "order"
        const val MONITOR_ID_FIELD = "monitor_id"
        const val CHAINED_FINDINGS_FIELD = "chained_findings"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Delegate {
            lateinit var monitorId: String
            var order = 0
            var chainedFindings: ChainedFindings? = null

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
                        chainedFindings = ChainedFindings.parse(xcp)
                    }
                }
            }
            return Delegate(order, monitorId, chainedFindings)
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
