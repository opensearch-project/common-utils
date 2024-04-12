package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import org.opensearch.search.builder.SearchSourceBuilder
import java.io.IOException

data class SearchInput(val indices: List<String>, val query: SearchSourceBuilder) : Input {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readStringList(), // indices
        SearchSourceBuilder(sin) // query
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .startObject(SEARCH_FIELD)
            .field(INDICES_FIELD, indices.toTypedArray())
            .field(QUERY_FIELD, query)
            .endObject()
            .endObject()
    }

    override fun name(): String {
        return SEARCH_FIELD
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeStringCollection(indices)
        query.writeTo(out)
    }

    companion object {
        const val INDICES_FIELD = "indices"
        const val QUERY_FIELD = "query"
        const val SEARCH_FIELD = "search"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("search"), CheckedFunction { parseInner(it) })

        @JvmStatic
        @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): SearchInput {
            val indices = mutableListOf<String>()
            lateinit var searchSourceBuilder: SearchSourceBuilder

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    INDICES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            indices.add(xcp.text())
                        }
                    }
                    QUERY_FIELD -> {
                        searchSourceBuilder = SearchSourceBuilder.fromXContent(xcp, false)
                    }
                }
            }

            return SearchInput(
                indices,
                requireNotNull(searchSourceBuilder) { "SearchInput query is null" }
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): SearchInput {
            return SearchInput(sin)
        }
    }
}
