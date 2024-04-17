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
import java.io.IOException

data class DocLevelMonitorInput(
    val description: String = NO_DESCRIPTION,
    val indices: List<String>,
    val queries: List<DocLevelQuery>,
    val iocFieldNames: List<String> = mutableListOf()
) : Input {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // description
        sin.readStringList(), // indices
        sin.readList(::DocLevelQuery), // docLevelQueries,
        sin.readStringList()
    )

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            DESCRIPTION_FIELD to description,
            INDICES_FIELD to indices,
            QUERIES_FIELD to queries.map { it.asTemplateArg() },
            IOC_FIELD_NAMES_FIELD to iocFieldNames
        )
    }

    override fun name(): String {
        return DOC_LEVEL_INPUT_FIELD
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(description)
        out.writeStringCollection(indices)
        out.writeCollection(queries)
        out.writeStringCollection(iocFieldNames)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .startObject(DOC_LEVEL_INPUT_FIELD)
            .field(DESCRIPTION_FIELD, description)
            .field(INDICES_FIELD, indices.toTypedArray())
            .field(QUERIES_FIELD, queries.toTypedArray())
            .field(IOC_FIELD_NAMES_FIELD, iocFieldNames.toTypedArray())
            .endObject()
            .endObject()
        return builder
    }

    companion object {
        const val DESCRIPTION_FIELD = "description"
        const val INDICES_FIELD = "indices"
        const val DOC_LEVEL_INPUT_FIELD = "doc_level_input"
        const val QUERIES_FIELD = "queries"
        const val IOC_FIELD_NAMES_FIELD = "query_field_names"

        const val NO_DESCRIPTION = ""

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Input::class.java,
            ParseField(DOC_LEVEL_INPUT_FIELD),
            CheckedFunction { parse(it) }
        )

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): DocLevelMonitorInput {
            var description: String = NO_DESCRIPTION
            val indices: MutableList<String> = mutableListOf()
            val docLevelQueries: MutableList<DocLevelQuery> = mutableListOf()
            val iocFieldNames: MutableList<String> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    DESCRIPTION_FIELD -> description = xcp.text()
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

                    QUERIES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            docLevelQueries.add(DocLevelQuery.parse(xcp))
                        }
                    }

                    IOC_FIELD_NAMES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val field = xcp.text()
                            iocFieldNames.add(field)
                        }
                    }
                }
            }

            return DocLevelMonitorInput(description = description, indices = indices, queries = docLevelQueries, iocFieldNames = iocFieldNames)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): DocLevelMonitorInput {
            return DocLevelMonitorInput(sin)
        }
    }

    constructor(
        description: String = NO_DESCRIPTION,
        indices: List<String>,
        queries: List<DocLevelQuery>
    ) : this(
        description,
        indices,
        queries,
        emptyList()
    )
}
