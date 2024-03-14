package org.opensearch.commons.alerting.model

import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.UUID

data class DocLevelQuery(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val query: String,
    val tags: List<String> = mutableListOf(),
    val queryFieldNames: List<String> = mutableListOf()
) : BaseModel {

    init {
        // Ensure the name and tags have valid characters
        validateQuery(name)
        for (tag in tags) {
            validateQuery(tag)
        }
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readString(), // name
        sin.readString(), // query
        sin.readStringList(), // tags,
        sin.readStringList() // fieldsBeingQueried
    )

    fun asTemplateArg(): Map<String, Any> {
        return mapOf(
            QUERY_ID_FIELD to id,
            NAME_FIELD to name,
            QUERY_FIELD to query,
            TAGS_FIELD to tags,
            QUERY_FIELD_NAMES_FIELD to queryFieldNames
        )
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeString(query)
        out.writeStringCollection(tags)
        out.writeStringCollection(queryFieldNames)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(QUERY_ID_FIELD, id)
            .field(NAME_FIELD, name)
            .field(QUERY_FIELD, query)
            .field(TAGS_FIELD, tags.toTypedArray())
            .field(QUERY_FIELD_NAMES_FIELD, queryFieldNames.toTypedArray())
            .endObject()
        return builder
    }

    companion object {
        const val QUERY_ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val QUERY_FIELD = "query"
        const val TAGS_FIELD = "tags"
        const val QUERY_FIELD_NAMES_FIELD = "query_field_names"
        const val NO_ID = ""
        val INVALID_CHARACTERS: List<String> = listOf(" ", "[", "]", "{", "}", "(", ")")

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): DocLevelQuery {
            var id: String = UUID.randomUUID().toString()
            lateinit var query: String
            lateinit var name: String
            val tags: MutableList<String> = mutableListOf()
            val queryFieldNames: MutableList<String> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    QUERY_ID_FIELD -> id = xcp.text()
                    NAME_FIELD -> {
                        name = xcp.text()
                        validateQuery(name)
                    }

                    QUERY_FIELD -> query = xcp.text()
                    TAGS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val tag = xcp.text()
                            validateQuery(tag)
                            tags.add(tag)
                        }
                    }

                    QUERY_FIELD_NAMES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val field = xcp.text()
                            queryFieldNames.add(field)
                        }
                    }
                }
            }

            return DocLevelQuery(
                id = id,
                name = name,
                query = query,
                tags = tags,
                queryFieldNames = queryFieldNames
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): DocLevelQuery {
            return DocLevelQuery(sin)
        }

        // TODO: add test for this
        private fun validateQuery(stringVal: String) {
            for (inValidChar in INVALID_CHARACTERS) {
                if (stringVal.contains(inValidChar)) {
                    throw IllegalArgumentException(
                        "They query name or tag, $stringVal, contains an invalid character: [' ','[',']','{','}','(',')']"
                    )
                }
            }
        }
    }

    // constructor for java plugins' convenience to optionally avoid passing empty list for 'fieldsBeingQueried' field
    constructor(
        id: String,
        name: String,
        query: String,
        tags: MutableList<String>
    ) : this(
        id = id,
        name = name,
        query = query,
        tags = tags,
        queryFieldNames = emptyList()
    )
}
