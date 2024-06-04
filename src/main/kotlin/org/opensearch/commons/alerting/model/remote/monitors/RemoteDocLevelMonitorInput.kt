package org.opensearch.commons.alerting.model.remote.monitors

import org.opensearch.commons.alerting.model.DocLevelMonitorInput
import org.opensearch.commons.alerting.model.DocLevelMonitorInput.Companion.DOC_LEVEL_INPUT_FIELD
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

data class RemoteDocLevelMonitorInput(val input: BytesReference, val docLevelMonitorInput: DocLevelMonitorInput) : Input {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readBytesReference(),
        DocLevelMonitorInput.readFrom(sin)
    )

    fun asTemplateArg(): Map<String, Any?> {
        val bytes = input.toBytesRef().bytes
        return mapOf(
            RemoteDocLevelMonitorInput.INPUT_SIZE to bytes.size,
            RemoteDocLevelMonitorInput.INPUT_FIELD to bytes,
            DOC_LEVEL_INPUT_FIELD to docLevelMonitorInput
        )
    }

    override fun name(): String {
        return REMOTE_DOC_LEVEL_MONITOR_INPUT_FIELD
    }

    override fun writeTo(out: StreamOutput) {
        out.writeBytesReference(input)
        docLevelMonitorInput.writeTo(out)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        val bytes = input.toBytesRef().bytes
        return builder.startObject()
            .startObject(REMOTE_DOC_LEVEL_MONITOR_INPUT_FIELD)
            .field(RemoteMonitorInput.INPUT_SIZE, bytes.size)
            .field(RemoteMonitorInput.INPUT_FIELD, bytes)
            .field(DOC_LEVEL_INPUT_FIELD, docLevelMonitorInput)
            .endObject()
            .endObject()
    }

    companion object {
        const val INPUT_FIELD = "input"
        const val INPUT_SIZE = "size"
        const val REMOTE_DOC_LEVEL_MONITOR_INPUT_FIELD = "remote_doc_level_monitor_input"

        fun parse(xcp: XContentParser): RemoteDocLevelMonitorInput {
            var bytes: ByteArray? = null
            var size: Int = 0
            var docLevelMonitorInput: DocLevelMonitorInput? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    RemoteMonitorInput.INPUT_FIELD -> bytes = xcp.binaryValue()
                    RemoteMonitorInput.INPUT_SIZE -> size = xcp.intValue()
                    Input.Type.DOCUMENT_LEVEL_INPUT.value -> docLevelMonitorInput = DocLevelMonitorInput.parse(xcp)
                }
            }
            val input = BytesReference.fromByteBuffer(ByteBuffer.wrap(bytes, 0, size))
            return RemoteDocLevelMonitorInput(input, docLevelMonitorInput!!)
        }
    }
}
