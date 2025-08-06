package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.commons.alerting.model.TriggerV2RunResult.Companion.ERROR_FIELD
import org.opensearch.commons.alerting.model.TriggerV2RunResult.Companion.NAME_FIELD
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

data class PPLTriggerRunResult(
    override var triggerName: String,
    override var error: Exception?,
    open var triggered: Boolean, // TODO: may need to change this based on whether trigger mode is result set or per result
    open var actionResults: MutableMap<String, ActionRunResult> = mutableMapOf()
) : TriggerV2RunResult {

    @Throws(IOException::class)
    @Suppress("UNCHECKED_CAST")
    constructor(sin: StreamInput) : this(
        triggerName = sin.readString(),
        error = sin.readException(),
        triggered = sin.readBoolean(),
        actionResults = sin.readMap() as MutableMap<String, ActionRunResult>
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field(NAME_FIELD, triggerName)

        builder.field(TRIGGERED_FIELD, triggered)
        builder.field(ACTION_RESULTS_FIELD, actionResults as Map<String, ActionRunResult>)

        val msg = error?.message
        builder.field(ERROR_FIELD, msg)
        builder.endObject()

        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(triggerName)
        out.writeException(error)
        out.writeBoolean(triggered)
        out.writeMap(actionResults as Map<String, ActionRunResult>)
    }

    override fun alertError(): AlertError? {
        if (error != null) {
            return AlertError(Instant.now(), "Failed evaluating trigger:\n${error!!.userErrorMessage()}")
        }
        for (actionResult in actionResults.values) {
            if (actionResult.error != null) {
                return AlertError(Instant.now(), "Failed running action:\n${actionResult.error.userErrorMessage()}")
            }
        }
        return null
    }

    companion object {
        const val TRIGGERED_FIELD = "triggered"
        const val ACTION_RESULTS_FIELD = "action_results"

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): TriggerRunResult {
            return QueryLevelTriggerRunResult(sin)
        }
    }
}