package org.opensearch.commons.alerting.model

import java.io.IOException
import java.time.Instant
import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder

interface TriggerV2RunResult : Writeable, ToXContent {

    val triggerName: String
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
        const val ERROR_FIELD = "error"
    }
}