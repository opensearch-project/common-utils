package org.opensearch.commons.alerting.model

import org.opensearch.commons.authuser.User
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.time.Instant

class CorrelationAlert : BaseAlert {
    // CorrelationAlert-specific properties
    val correlatedFindingIds: List<String>
    val correlationRuleId: String
    val correlationRuleName: String

    constructor(
        correlatedFindingIds: List<String>,
        correlationRuleId: String,
        correlationRuleName: String,
        id: String,
        version: Long,
        schemaVersion: Int,
        user: User?,
        triggerName: String,
        state: Alert.State,
        startTime: Instant,
        endTime: Instant?,
        acknowledgedTime: Instant?,
        errorMessage: String?,
        severity: String,
        actionExecutionResults: List<ActionExecutionResult>,
    ) : super(
        id = id,
        version = version,
        schemaVersion = schemaVersion,
        user = user,
        triggerName = triggerName,
        state = state,
        startTime = startTime,
        endTime = endTime,
        acknowledgedTime = acknowledgedTime,
        errorMessage = errorMessage,
        severity = severity,
        actionExecutionResults = actionExecutionResults,
    ) {
        this.correlatedFindingIds = correlatedFindingIds
        this.correlationRuleId = correlationRuleId
        this.correlationRuleName = correlationRuleName
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super(sin) {
        correlatedFindingIds = sin.readStringList()
        correlationRuleId = sin.readString()
        correlationRuleName = sin.readString()
    }

    // Override to include CorrelationAlert specific fields
    override fun toXContent(
        builder: XContentBuilder,
        params: ToXContent.Params,
    ): XContentBuilder {
        builder
            .startObject()
            .startArray(CORRELATED_FINDING_IDS)
        correlatedFindingIds.forEach { id ->
            builder.value(id)
        }
        builder
            .endArray()
            .field(CORRELATION_RULE_ID, correlationRuleId)
            .field(CORRELATION_RULE_NAME, correlationRuleName)
        super.toXContentWithUser(builder)
        builder.endObject()
        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
        out.writeStringCollection(correlatedFindingIds)
        out.writeString(correlationRuleId)
        out.writeString(correlationRuleName)
    }

    override fun asTemplateArg(): Map<String, Any?> {
        val superTemplateArgs = super.asTemplateArg()
        val correlationSpecificArgs =
            mapOf(
                CORRELATED_FINDING_IDS to correlatedFindingIds,
                CORRELATION_RULE_ID to correlationRuleId,
                CORRELATION_RULE_NAME to correlationRuleName,
            )
        return superTemplateArgs + correlationSpecificArgs
    }

    companion object {
        const val CORRELATED_FINDING_IDS = "correlated_finding_ids"
        const val CORRELATION_RULE_ID = "correlation_rule_id"
        const val CORRELATION_RULE_NAME = "correlation_rule_name"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(
            xcp: XContentParser,
            id: String = NO_ID,
            version: Long = NO_VERSION,
        ): CorrelationAlert {
            // Parse additional CorrelationAlert-specific fields
            val correlatedFindingIds: MutableList<String> = mutableListOf()
            var correlationRuleId: String? = null
            var correlationRuleName: String? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    CORRELATED_FINDING_IDS -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            correlatedFindingIds.add(xcp.text())
                        }
                    }

                    CORRELATION_RULE_ID -> {
                        correlationRuleId = xcp.text()
                    }

                    CORRELATION_RULE_NAME -> {
                        correlationRuleName = xcp.text()
                    }
                }
            }

            val unifiedAlert = parse(xcp, version)
            return CorrelationAlert(
                correlatedFindingIds = correlatedFindingIds,
                correlationRuleId = requireNotNull(correlationRuleId),
                correlationRuleName = requireNotNull(correlationRuleName),
                id = requireNotNull(unifiedAlert.id),
                version = requireNotNull(unifiedAlert.version),
                schemaVersion = requireNotNull(unifiedAlert.schemaVersion),
                user = unifiedAlert.user,
                triggerName = requireNotNull(unifiedAlert.triggerName),
                state = requireNotNull(unifiedAlert.state),
                startTime = requireNotNull(unifiedAlert.startTime),
                endTime = unifiedAlert.endTime,
                acknowledgedTime = unifiedAlert.acknowledgedTime,
                errorMessage = unifiedAlert.errorMessage,
                severity = requireNotNull(unifiedAlert.severity),
                actionExecutionResults = unifiedAlert.actionExecutionResults,
            )
        }
    }
}
