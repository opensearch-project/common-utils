/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.model

import org.opensearch.core.common.bytes.BytesReference
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

class IdDocPair : ToXContentObject, Writeable {
    var docId: String
    var document: BytesReference

    constructor(docId: String, document: BytesReference) : super() {
        this.docId = docId
        this.document = document
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        docId = sin.readString(),
        document = sin.readBytesReference()
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(docId)
        out.writeBytesReference(document)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field("docId", docId)
            .field("document", document)
        return builder.endObject()
    }

    @Throws(IOException::class)
    fun readFrom(sin: StreamInput): IdDocPair {
        return IdDocPair(sin)
    }

    companion object {
        const val DOC_ID_FIELD = "docId"
        const val DOCUMENT_FIELD = "document"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): IdDocPair {
            var docId: String? = null
            var document: BytesReference? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    DOC_ID_FIELD -> docId = xcp.text()
                    DOCUMENT_FIELD -> {
                        val xContentBuilder = XContentBuilder.builder(xcp.contentType().xContent())
                        xContentBuilder.copyCurrentStructure(xcp)
                        document = BytesReference.bytes(xContentBuilder)
                    }

                    else -> {
                        xcp.skipChildren()
                    }
                }
            }

            return IdDocPair(
                requireNotNull(docId) { "docId is null" },
                requireNotNull(document) { "document is null" }
            )
        }
    }
}
