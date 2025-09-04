package org.opensearch.commons.alerting.model

import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import java.time.Instant

interface TriggerV2RunResult : Writeable, ToXContent {

    val triggerName: String
    val triggered: Boolean
    val error: Exception?

    /** Returns error information to store in the Alert. Currently it's just the stack trace but it can be more */
    fun alertError(): AlertError? {
        if (error != null) {
            return AlertError(Instant.now(), "Failed evaluating trigger:\n${error!!.userErrorMessage()}")
        }
        return null
    }

    companion object {
        const val NAME_FIELD = "name"
        const val TRIGGERED_FIELD = "triggered"
        const val ERROR_FIELD = "error"
    }
}
