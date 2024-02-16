/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.model

import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

class StreamingIndex : ToXContentObject, Writeable {
    var index: String
    var idDocPairs: List<IdDocPair>

    constructor(index: String, idDocPairs: List<IdDocPair>) : super() {
        this.index = index
        this.idDocPairs = idDocPairs
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        index = sin.readString(),
        idDocPairs = sin.readList(::IdDocPair)
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(index)
        out.writeCollection(idDocPairs)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field("index", index)
            .field("idDocPairs", idDocPairs)
        return builder.endObject()
    }

    @Throws(IOException::class)
    fun readFrom(sin: StreamInput): StreamingIndex {
        return StreamingIndex(sin)
    }

    companion object {
        const val INDEX_FIELD = "index"
        const val ID_DOC_PAIRS_FIELD = "idDocPairs"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): StreamingIndex {
            var index: String? = null
            var idDocPairs: MutableList<IdDocPair> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    INDEX_FIELD -> index = xcp.text()
                    ID_DOC_PAIRS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val idDocPair = IdDocPair.parse(xcp)
                            idDocPairs.add(idDocPair)
                        }
                    }

                    else -> {
                        xcp.skipChildren()
                    }
                }
            }

            return StreamingIndex(
                requireNotNull(index) { "index is null" },
                idDocPairs
            )
        }
    }
}
