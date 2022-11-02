package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

data class DataSources(
    /** Configures a custom query index name for the monitor. Creates a new index if index with given name not present.*/
    val queryIndex: String = ScheduledJob.DOC_LEVEL_QUERIES_INDEX,

    /** Configures a custom index to store findings for a monitor. Creates a new index if index with given name not present.
     *  If index is pre-existing, mapping is updated*/
    val findingsIndex: String = ".opensearch-alerting-finding-history-write", // AlertIndices.FINDING_HISTORY_WRITE_INDEX

    /** Configures a custom index pattern for  findingsIndex alias.*/
    val findingsIndexPattern: String? = "<.opensearch-alerting-finding-history-{now/d}-1>", // AlertIndices.FINDING_HISTORY_INDEX_PATTERN

    /** Configures a custom index to store alerts for a monitor. Creates a new index if index with given name not present.
     *  If index is pre-existing, mapping is updated. */
    val alertsIndex: String = ".opendistro-alerting-alerts", // AlertIndices.ALERT_INDEX

    /** Configures a custom index alias to store historic alerts for a monitor.*/
    val alertsHistoryIndex: String? = ".opendistro-alerting-alert-history-write", // AlertIndices.ALERT_HISTORY_WRITE_INDEX

    /** Configures a custom index pattern for alertHistoryIndex alias.*/
    val alertsHistoryIndexPattern: String? = "<.opendistro-alerting-alert-history-{now/d}-1>", // AlertIndices.ALERT_HISTORY_INDEX_PATTERN

    /** Configures custom mappings by field type for query index.
     * Custom query index mappings are configurable, only if a custom query index is configured too. */
    val queryIndexMappingsByType: Map<String, Map<String, String>> = mapOf(),

    /** Configures flag to enable or disable creating and storing findings. */
    val findingsEnabled: Boolean? = false

) : Writeable, ToXContentObject {

    init {
        require(queryIndex.isNotEmpty()) {
            "Query index cannot be empty"
        }
        require(findingsIndex.isNotEmpty()) {
            "Findings index cannot be empty"
        }
        require(alertsIndex.isNotEmpty()) {
            "Alerts index cannot be empty"
        }
        if (queryIndexMappingsByType.isNotEmpty()) {
            require(queryIndex != ScheduledJob.DOC_LEVEL_QUERIES_INDEX) {
                "Custom query index mappings are configurable only if a custom query index is configured too."
            }
            require(
                queryIndexMappingsByType.size == 1 &&
                    queryIndexMappingsByType.containsKey("text") &&
                    queryIndexMappingsByType.get("text")?.size == 1 &&
                    queryIndexMappingsByType.get("text")!!.containsKey("analyzer")
            ) {
                "Custom query index mappings are currently configurable only for 'text' fields and mapping parameter can only be 'analyzer'"
            }
        }
    }

    @Throws(IOException::class)
    @Suppress("UNCHECKED_CAST")
    constructor(sin: StreamInput) : this(
        queryIndex = sin.readString(),
        findingsIndex = sin.readString(),
        findingsIndexPattern = sin.readOptionalString(),
        alertsIndex = sin.readString(),
        alertsHistoryIndex = sin.readOptionalString(),
        alertsHistoryIndexPattern = sin.readOptionalString(),
        queryIndexMappingsByType = sin.readMap() as Map<String, Map<String, String>>,
        findingsEnabled = sin.readOptionalBoolean()
    )

    @Suppress("UNCHECKED_CAST")
    fun asTemplateArg(): Map<String, Any?> {
        return mapOf(
            QUERY_INDEX_FIELD to queryIndex,
            FINDINGS_INDEX_FIELD to findingsIndex,
            FINDINGS_INDEX_PATTERN_FIELD to findingsIndexPattern,
            ALERTS_INDEX_FIELD to alertsIndex,
            ALERTS_HISTORY_INDEX_FIELD to alertsHistoryIndex,
            ALERTS_HISTORY_INDEX_PATTERN_FIELD to alertsHistoryIndexPattern,
            QUERY_INDEX_MAPPINGS_BY_TYPE to queryIndexMappingsByType,
            FINDINGS_ENABLED_FIELD to findingsEnabled,
        )
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field(QUERY_INDEX_FIELD, queryIndex)
        builder.field(FINDINGS_INDEX_FIELD, findingsIndex)
        builder.field(FINDINGS_INDEX_PATTERN_FIELD, findingsIndexPattern)
        builder.field(ALERTS_INDEX_FIELD, alertsIndex)
        builder.field(ALERTS_HISTORY_INDEX_FIELD, alertsHistoryIndex)
        builder.field(ALERTS_HISTORY_INDEX_PATTERN_FIELD, alertsHistoryIndexPattern)
        builder.field(QUERY_INDEX_MAPPINGS_BY_TYPE, queryIndexMappingsByType as Map<String, Any>)
        builder.field(FINDINGS_ENABLED_FIELD, findingsEnabled)
        builder.endObject()
        return builder
    }

    companion object {
        const val QUERY_INDEX_FIELD = "query_index"
        const val FINDINGS_INDEX_FIELD = "findings_index"
        const val FINDINGS_INDEX_PATTERN_FIELD = "findings_index_pattern"
        const val ALERTS_INDEX_FIELD = "alerts_index"
        const val ALERTS_HISTORY_INDEX_FIELD = "alerts_history_index"
        const val ALERTS_HISTORY_INDEX_PATTERN_FIELD = "alerts_history_index_pattern"
        const val QUERY_INDEX_MAPPINGS_BY_TYPE = "query_index_mappings_by_type"
        const val FINDINGS_ENABLED_FIELD = "findings_enabled"

        @JvmStatic
        @Throws(IOException::class)
        @Suppress("UNCHECKED_CAST")
        fun parse(xcp: XContentParser): DataSources {
            var queryIndex = ""
            var findingsIndex = ""
            var findingsIndexPattern = ""
            var alertsIndex = ""
            var alertsHistoryIndex = ""
            var alertsHistoryIndexPattern = ""
            var queryIndexMappingsByType: Map<String, Map<String, String>> = mapOf()
            var findingsEnabled = false

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    QUERY_INDEX_FIELD -> queryIndex = xcp.text()
                    FINDINGS_INDEX_FIELD -> findingsIndex = xcp.text()
                    FINDINGS_INDEX_PATTERN_FIELD -> findingsIndexPattern = xcp.text()
                    ALERTS_INDEX_FIELD -> alertsIndex = xcp.text()
                    ALERTS_HISTORY_INDEX_FIELD -> alertsHistoryIndex = xcp.text()
                    ALERTS_HISTORY_INDEX_PATTERN_FIELD -> alertsHistoryIndexPattern = xcp.text()
                    QUERY_INDEX_MAPPINGS_BY_TYPE -> queryIndexMappingsByType = xcp.map() as Map<String, Map<String, String>>
                    FINDINGS_ENABLED_FIELD -> findingsEnabled = xcp.booleanValue()
                }
            }
            return DataSources(
                queryIndex = queryIndex,
                findingsIndex = findingsIndex,
                findingsIndexPattern = findingsIndexPattern,
                alertsIndex = alertsIndex,
                alertsHistoryIndex = alertsHistoryIndex,
                alertsHistoryIndexPattern = alertsHistoryIndexPattern,
                queryIndexMappingsByType = queryIndexMappingsByType,
                findingsEnabled = findingsEnabled
            )
        }
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(queryIndex)
        out.writeString(findingsIndex)
        out.writeOptionalString(findingsIndexPattern)
        out.writeString(alertsIndex)
        out.writeOptionalString(alertsHistoryIndex)
        out.writeOptionalString(alertsHistoryIndexPattern)
        out.writeMap(queryIndexMappingsByType as Map<String, Any>)
        out.writeOptionalBoolean(findingsEnabled)
    }
}
