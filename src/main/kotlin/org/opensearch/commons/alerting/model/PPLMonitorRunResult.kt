package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.ERROR_FIELD
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.MONITOR_NAME_FIELD
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.PERIOD_END_FIELD
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.PERIOD_START_FIELD
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.TRIGGER_RESULTS_FIELD
import org.opensearch.commons.alerting.model.MonitorV2RunResult.Companion.suppressWarning
import org.opensearch.commons.alerting.util.nonOptionalTimeField
import org.opensearch.commons.alerting.util.optionalTimeField
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

data class PPLMonitorRunResult(
    override val monitorName: String,
    override val error: Exception?,
    override val periodStart: Instant,
    override val periodEnd: Instant,
    override val triggerResults: Map<String, PPLTriggerRunResult>,
    val pplQueryResults: String // TODO: will likely be a different type like Map or JsonObject
) : MonitorV2RunResult<PPLTriggerRunResult> {

    @Throws(IOException::class)
    @Suppress("UNCHECKED_CAST")
    constructor(sin: StreamInput) : this(
        sin.readString(), // monitorName
        sin.readException(), // error
        sin.readInstant(), // periodStart
        sin.readInstant(), // periodEnd
        suppressWarning(sin.readMap()) as Map<String, PPLTriggerRunResult>, // triggerResults
        sin.readString() // pplQueryResults
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field(MONITOR_NAME_FIELD, monitorName)
        builder.nonOptionalTimeField(PERIOD_START_FIELD, periodStart)
        builder.nonOptionalTimeField(PERIOD_END_FIELD, periodEnd)
        builder.field(ERROR_FIELD, error?.message)
        builder.field(TRIGGER_RESULTS_FIELD, triggerResults)
        builder.field(PPL_QUERY_RESULTS_FIELD, pplQueryResults)
        builder.endObject()
        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorName)
        out.writeException(error)
        out.writeInstant(periodStart)
        out.writeInstant(periodEnd)
        out.writeMap(triggerResults)
        out.writeString(pplQueryResults)
    }

    // TODO: does this need any PPLMonitor specific logic, or can this just be deleted
    override fun alertError(): AlertError? {
        if (error != null) {
            return AlertError(Instant.now(), "Failed running monitor:\n${error.userErrorMessage()}")
        }

        return null
    }

    companion object {
        const val PPL_QUERY_RESULTS_FIELD = "ppl_query_results"
    }
}