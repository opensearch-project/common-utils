package org.opensearch.commons.alerting.model

import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.commons.utils.validateId
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.util.Collections

// TODO - Remove the class and move the monitorId to Delegate (as a chainedMonitorId property) if this class won't be updated by adding new properties

/**
 * Context passed in delegate monitor to filter data matched by a list of monitors based on the findings of the given monitor ids.
 */
data class ChainedMonitorFindings(
    val monitorId: String? = null,
    val monitorIds: List<String> = emptyList(), // if monitorId field is non-null it would be given precendence for BWC
) : BaseModel {
    init {
        require(!(monitorId.isNullOrBlank() && monitorIds.isEmpty())) {
            "at least one of fields, 'monitorIds' and 'monitorId' should be provided"
        }
        if (monitorId != null && monitorId.isBlank()) {
            validateId(monitorId)
        } else {
            monitorIds.forEach { validateId(it) }
        }
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readOptionalString(), // monitorId
        Collections.unmodifiableList(sin.readStringList()),
    )

    @Suppress("UNCHECKED_CAST")
    fun asTemplateArg(): Map<String, Any> =
        mapOf(
            MONITOR_ID_FIELD to monitorId,
            MONITOR_IDS_FIELD to monitorIds,
        ) as Map<String, Any>

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeOptionalString(monitorId)
        out.writeStringCollection(monitorIds)
    }

    override fun toXContent(
        builder: XContentBuilder,
        params: ToXContent.Params,
    ): XContentBuilder {
        builder
            .startObject()
            .field(MONITOR_ID_FIELD, monitorId)
            .field(MONITOR_IDS_FIELD, monitorIds)
            .endObject()
        return builder
    }

    companion object {
        const val MONITOR_ID_FIELD = "monitor_id"
        const val MONITOR_IDS_FIELD = "monitor_ids"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): ChainedMonitorFindings {
            var monitorId: String? = null
            val monitorIds = mutableListOf<String>()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    MONITOR_ID_FIELD -> {
                        if (!xcp.currentToken().equals(XContentParser.Token.VALUE_NULL)) {
                            monitorId = xcp.text()
                        }
                    }

                    MONITOR_IDS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp,
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            monitorIds.add(xcp.text())
                        }
                    }
                }
            }
            return ChainedMonitorFindings(monitorId, monitorIds)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): ChainedMonitorFindings = ChainedMonitorFindings(sin)
    }
}
