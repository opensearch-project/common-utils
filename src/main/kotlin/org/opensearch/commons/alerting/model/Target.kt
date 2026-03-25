package org.opensearch.commons.alerting.model

import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Describes where a monitor query executes.
 *
 * @property type the target type — LOCAL by default. Extensible for additional target types.
 * @property endpoint the URL of the remote target. Required when type is not LOCAL.
 */
data class Target(
    val type: String = LOCAL,
    val endpoint: String = ""
) : Writeable, ToXContent {

    init {
        require(type.isNotBlank()) { "target type cannot be empty" }
        if (type != LOCAL) {
            require(endpoint.isNotBlank()) { "endpoint is required when target type is not LOCAL" }
        }
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        type = sin.readString(),
        endpoint = sin.readString()
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(TYPE_FIELD, type)
            .field(ENDPOINT_FIELD, endpoint)
        return builder.endObject()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(type)
        out.writeString(endpoint)
    }

    companion object {
        const val TYPE_FIELD = "type"
        const val ENDPOINT_FIELD = "endpoint"
        const val LOCAL = "local"
        val DEFAULT = Target()

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Target {
            var type = LOCAL
            var endpoint = ""

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    TYPE_FIELD -> type = xcp.text()
                    ENDPOINT_FIELD -> endpoint = xcp.text()
                }
            }
            return Target(type, endpoint)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Target = Target(sin)
    }
}
