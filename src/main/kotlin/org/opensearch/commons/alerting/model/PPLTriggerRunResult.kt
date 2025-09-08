package org.opensearch.commons.alerting.model

import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.commons.alerting.model.TriggerV2RunResult.Companion.ERROR_FIELD
import org.opensearch.commons.alerting.model.TriggerV2RunResult.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.TriggerV2RunResult.Companion.TRIGGERED_FIELD
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException
import java.time.Instant

data class PPLTriggerRunResult(
    override var triggerName: String,
    override var triggered: Boolean,
    override var error: Exception?,
) : TriggerV2RunResult {

    @Throws(IOException::class)
    @Suppress("UNCHECKED_CAST")
    constructor(sin: StreamInput) : this(
        triggerName = sin.readString(),
        triggered = sin.readBoolean(),
        error = sin.readException()
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field(NAME_FIELD, triggerName)
        builder.field(TRIGGERED_FIELD, triggered)
        builder.field(ERROR_FIELD, error?.message)
        builder.endObject()
        return builder
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(triggerName)
        out.writeBoolean(triggered)
        out.writeException(error)
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): TriggerRunResult {
            return QueryLevelTriggerRunResult(sin)
        }
    }
}
