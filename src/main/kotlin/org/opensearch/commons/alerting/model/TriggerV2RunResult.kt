package org.opensearch.commons.alerting.model

import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import java.time.Instant

interface TriggerV2RunResult : Writeable, ToXContent {

    val triggerName: String
    val triggered: Boolean
    val error: Exception?

    companion object {
        const val NAME_FIELD = "name"
        const val TRIGGERED_FIELD = "triggered"
        const val ERROR_FIELD = "error"
    }
}
