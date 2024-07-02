package org.opensearch.commons.alerting.model.remote.monitors

import org.opensearch.commons.alerting.model.Input
import org.opensearch.core.common.bytes.BytesReference
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.nio.ByteBuffer

data class RemoteMonitorInput(val input: BytesReference) : Input {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readBytesReference()
    )

    fun asTemplateArg(): Map<String, Any?> {
        val bytes = input.toBytesRef().bytes
        return mapOf(
            INPUT_SIZE to bytes.size,
            INPUT_FIELD to bytes
        )
    }

    override fun name(): String {
        return REMOTE_MONITOR_INPUT_FIELD
    }

    override fun writeTo(out: StreamOutput) {
        out.writeBytesReference(input)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        val bytes = input.toBytesRef().bytes
        return builder.startObject()
            .startObject(REMOTE_MONITOR_INPUT_FIELD)
            .field(INPUT_SIZE, bytes.size)
            .field(INPUT_FIELD, bytes)
            .endObject()
            .endObject()
    }

    companion object {
        const val INPUT_FIELD = "input"
        const val INPUT_SIZE = "size"
        const val REMOTE_MONITOR_INPUT_FIELD = "remote_monitor_input"

        fun parse(xcp: XContentParser): RemoteMonitorInput {
            var bytes: ByteArray? = null
            var size: Int = 0

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    INPUT_FIELD -> bytes = xcp.binaryValue()
                    INPUT_SIZE -> size = xcp.intValue()
                }
            }
            val input = BytesReference.fromByteBuffer(ByteBuffer.wrap(bytes, 0, size))
            return RemoteMonitorInput(input)
        }
    }
}
