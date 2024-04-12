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
    val fields: List<String>,
    val query: String,
    val tags: List<String> = mutableListOf()
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
        sin.readStringList(), // fields
        sin.readString(), // query
        sin.readStringList() // tags
    )

    fun asTemplateArg(): Map<String, Any> {
        return mapOf(
            QUERY_ID_FIELD to id,
            NAME_FIELD to name,
            FIELDS_FIELD to fields,
            QUERY_FIELD to query,
            TAGS_FIELD to tags
        )
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeStringCollection(fields)
        out.writeString(query)
        out.writeStringCollection(tags)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(QUERY_ID_FIELD, id)
            .field(NAME_FIELD, name)
            .field(FIELDS_FIELD, fields.toTypedArray())
            .field(QUERY_FIELD, query)
            .field(TAGS_FIELD, tags.toTypedArray())
            .endObject()
        return builder
    }

    companion object {
        const val QUERY_ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val FIELDS_FIELD = "fields"
        const val QUERY_FIELD = "query"
        const val TAGS_FIELD = "tags"
        const val NO_ID = ""
        val INVALID_CHARACTERS: List<String> = listOf(" ", "[", "]", "{", "}", "(", ")")

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): DocLevelQuery {
            var id: String = UUID.randomUUID().toString()
            lateinit var query: String
            lateinit var name: String
            val tags: MutableList<String> = mutableListOf()
            val fields: MutableList<String> = mutableListOf()

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
                    FIELDS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val field = xcp.text()
                            fields.add(field)
                        }
                    }
                }
            }

            return DocLevelQuery(
                id = id,
                name = name,
                fields = fields,
                query = query,
                tags = tags
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
}
