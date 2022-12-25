package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.model.BaseModel
import java.io.IOException

data class Delegate(
    val order: Int,
    val monitorId: String,
) : BaseModel {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readInt(), // order
        sin.readString(), // monitorId
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
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(ORDER_FIELD, order)
            .field(MONITOR_ID_FIELD, monitorId)
            .endObject()
        return builder
    }

    companion object {
        const val ORDER_FIELD = "order"
        const val MONITOR_ID_FIELD = "monitor_id"

        @JvmStatic @Throws(IOException::class)
        fun parse(xcp: XContentParser): Delegate {
//            var id: String = UUID.randomUUID().toString()
//            lateinit var query: String
//            lateinit var name: String
//            val tags: MutableList<String> = mutableListOf()
//
//            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
//            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
//                val fieldName = xcp.currentName()
//                xcp.nextToken()
//
//                when (fieldName) {
//                    QUERY_ID_FIELD -> id = xcp.text()
//                    NAME_FIELD -> {
//                        name = xcp.text()
//                        validateQuery(name)
//                    }
//                    QUERY_FIELD -> query = xcp.text()
//                    TAGS_FIELD -> {
//                        XContentParserUtils.ensureExpectedToken(
//                            XContentParser.Token.START_ARRAY,
//                            xcp.currentToken(),
//                            xcp
//                        )
//                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
//                            val tag = xcp.text()
//                            validateQuery(tag)
//                            tags.add(tag)
//                        }
//                    }
//                }
//            }

            return Delegate(1,"")
        }

        @JvmStatic @Throws(IOException::class)
        fun readFrom(sin: StreamInput): DocLevelQuery {
            return Delegate(sin)
        }

    }
}
