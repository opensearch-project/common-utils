package org.opensearch.commons.alerting.model

import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

data class PPLSQLTriggerRunResult(
    override var triggerName: String,
    override var error: Exception?,
    var triggered: Boolean
) : TriggerRunResult(triggerName, error) {

    @Throws(IOException::class)
    @Suppress("UNCHECKED_CAST")
    constructor(sin: StreamInput) : this(
        triggerName = sin.readString(),
        error = sin.readException(),
        triggered = sin.readBoolean()
    )

    override fun internalXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.field(TRIGGERED_FIELD, triggered)
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
        out.writeBoolean(triggered)
    }

    companion object {
        const val TRIGGERED_FIELD = "triggered"

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): TriggerRunResult {
            return PPLSQLTriggerRunResult(sin)
        }
    }
}
