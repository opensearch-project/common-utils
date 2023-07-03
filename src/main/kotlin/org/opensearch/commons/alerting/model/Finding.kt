package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.opensearch.commons.alerting.util.instant
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException
import java.time.Instant

/**
 * A wrapper of the log event that enriches the event by also including information about the monitor it triggered.
 */
class Finding(
    val id: String = NO_ID,
    val relatedDocIds: List<String>,
    val correlatedDocIds: List<String> = listOf(),
    val monitorId: String,
    val monitorName: String,
    val index: String,
    val docLevelQueries: List<DocLevelQuery>,
    val timestamp: Instant,
    /**
     * Keeps the track of the workflow-monitor exact execution.
     * Used for filtering the data when chaining monitors in a workflow.
     */
    val executionId: String? = null,
) : Writeable, ToXContent {

    constructor(
        id: String = NO_ID,
        relatedDocIds: List<String>,
        monitorId: String,
        monitorName: String,
        index: String,
        docLevelQueries: List<DocLevelQuery>,
        timestamp: Instant
    ) : this (
        id = id,
        relatedDocIds = relatedDocIds,
        monitorId = monitorId,
        monitorName = monitorName,
        index = index,
        docLevelQueries = docLevelQueries,
        timestamp = timestamp,
        executionId = null
    )

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        id = sin.readString(),
        relatedDocIds = sin.readStringList(),
        correlatedDocIds = sin.readStringList(),
        monitorId = sin.readString(),
        monitorName = sin.readString(),
        index = sin.readString(),
        docLevelQueries = sin.readList((DocLevelQuery)::readFrom),
        timestamp = sin.readInstant(),
        executionId = sin.readOptionalString()
    )

    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            FINDING_ID_FIELD to id,
            RELATED_DOC_IDS_FIELD to relatedDocIds,
            CORRELATED_DOC_IDS_FIELD to correlatedDocIds,
            MONITOR_ID_FIELD to monitorId,
            MONITOR_NAME_FIELD to monitorName,
            INDEX_FIELD to index,
            QUERIES_FIELD to docLevelQueries,
            TIMESTAMP_FIELD to timestamp.toEpochMilli(),
            EXECUTION_ID_FIELD to executionId
        )
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(FINDING_ID_FIELD, id)
            .field(RELATED_DOC_IDS_FIELD, relatedDocIds)
            .field(CORRELATED_DOC_IDS_FIELD, correlatedDocIds)
            .field(MONITOR_ID_FIELD, monitorId)
            .field(MONITOR_NAME_FIELD, monitorName)
            .field(INDEX_FIELD, index)
            .field(QUERIES_FIELD, docLevelQueries.toTypedArray())
            .field(TIMESTAMP_FIELD, timestamp.toEpochMilli())
            .field(EXECUTION_ID_FIELD, executionId)
        builder.endObject()
        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeStringCollection(relatedDocIds)
        out.writeStringCollection(correlatedDocIds)
        out.writeString(monitorId)
        out.writeString(monitorName)
        out.writeString(index)
        out.writeCollection(docLevelQueries)
        out.writeInstant(timestamp)
        out.writeOptionalString(executionId)
    }

    companion object {
        const val FINDING_ID_FIELD = "id"
        const val RELATED_DOC_IDS_FIELD = "related_doc_ids"
        const val CORRELATED_DOC_IDS_FIELD = "correlated_doc_ids"
        const val MONITOR_ID_FIELD = "monitor_id"
        const val MONITOR_NAME_FIELD = "monitor_name"
        const val INDEX_FIELD = "index"
        const val QUERIES_FIELD = "queries"
        const val TIMESTAMP_FIELD = "timestamp"
        const val EXECUTION_ID_FIELD = "execution_id"
        const val NO_ID = ""

        @JvmStatic @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Finding {
            var id: String = NO_ID
            val relatedDocIds: MutableList<String> = mutableListOf()
            val correlatedDocIds: MutableList<String> = mutableListOf()
            lateinit var monitorId: String
            lateinit var monitorName: String
            lateinit var index: String
            val queries: MutableList<DocLevelQuery> = mutableListOf()
            lateinit var timestamp: Instant
            var executionId: String? = null

            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    FINDING_ID_FIELD -> id = xcp.text()
                    RELATED_DOC_IDS_FIELD -> {
                        ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            relatedDocIds.add(xcp.text())
                        }
                    }
                    CORRELATED_DOC_IDS_FIELD -> {
                        ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            correlatedDocIds.add(xcp.text())
                        }
                    }
                    MONITOR_ID_FIELD -> monitorId = xcp.text()
                    MONITOR_NAME_FIELD -> monitorName = xcp.text()
                    INDEX_FIELD -> index = xcp.text()
                    QUERIES_FIELD -> {
                        ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            queries.add(DocLevelQuery.parse(xcp))
                        }
                    }
                    TIMESTAMP_FIELD -> {
                        timestamp = requireNotNull(xcp.instant())
                    }
                    EXECUTION_ID_FIELD -> executionId = xcp.textOrNull()
                }
            }

            return Finding(
                id = id,
                relatedDocIds = relatedDocIds,
                correlatedDocIds = correlatedDocIds,
                monitorId = monitorId,
                monitorName = monitorName,
                index = index,
                docLevelQueries = queries,
                timestamp = timestamp,
                executionId = executionId
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Finding {
            return Finding(sin)
        }
    }
}
