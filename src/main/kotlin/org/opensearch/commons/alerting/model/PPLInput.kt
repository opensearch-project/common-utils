package org.opensearch.commons.alerting.model

import org.opensearch.common.CheckedFunction
import org.opensearch.commons.alerting.util.AlertingException
import org.opensearch.core.ParseField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

data class PPLInput(
    val query: String,
    val queryLanguage: QueryLanguage = QueryLanguage.PPL
) : Input {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // query
        sin.readEnum(QueryLanguage::class.java)
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(query)
        out.writeEnum(queryLanguage)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .startObject(PPL_INPUT_FIELD)
            .field(QUERY_FIELD, query)
            .field(QUERY_LANGUAGE_FIELD, queryLanguage.value)
            .endObject()
            .endObject()
    }

    override fun asTemplateArg(): Map<String, Any> =
        mapOf(
            PPL_INPUT_FIELD to mapOf(
                QUERY_FIELD to query,
                QUERY_LANGUAGE_FIELD to queryLanguage.value
            )
        )

    override fun name(): String = PPL_INPUT_FIELD

    enum class QueryLanguage(val value: String) {
        PPL(PPL_QUERY_LANGUAGE),
        SQL(SQL_QUERY_LANGUAGE);

        companion object {
            fun enumFromString(value: String): QueryLanguage? = QueryLanguage.entries.firstOrNull { it.value == value }
        }
    }

    companion object {
        // PPL Input field names
        const val PPL_INPUT_FIELD = "ppl_input"
        const val QUERY_FIELD = "query"
        const val QUERY_LANGUAGE_FIELD = "query_language"

        // query languages
        const val PPL_QUERY_LANGUAGE = "ppl"
        const val SQL_QUERY_LANGUAGE = "sql"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Input::class.java,
            ParseField(PPL_INPUT_FIELD),
            CheckedFunction { parseInner(it) }
        )

        @JvmStatic
        @Throws(IOException::class)
        fun parseInner(xcp: XContentParser): PPLInput {
            lateinit var query: String
            var queryLanguage: QueryLanguage = QueryLanguage.PPL // default to PPL

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    QUERY_FIELD -> query = xcp.text()
                    QUERY_LANGUAGE_FIELD -> {
                        val input = xcp.text()
                        val enumMatchResult = QueryLanguage.enumFromString(input)
                            ?: throw AlertingException.wrap(
                                IllegalArgumentException(
                                    "Invalid value for $QUERY_LANGUAGE_FIELD: $input. " +
                                        "Supported values are ${QueryLanguage.entries.map { it.value }}"
                                )
                            )
                        queryLanguage = enumMatchResult
                    }
                }
            }
            return PPLInput(query, queryLanguage)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): PPLInput {
            return PPLInput(sin)
        }
    }
}
