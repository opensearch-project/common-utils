package org.opensearch.commons.alerting.util

import org.opensearch.common.bytes.BytesReference
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.settings.SupportedClusterMetricsSettings
import org.opensearch.commons.authuser.User
import java.time.Instant

class IndexUtils {
    companion object {
        const val NO_SCHEMA_VERSION = 0

        const val MONITOR_MAX_INPUTS = 1

        const val MONITOR_MAX_TRIGGERS = 10

        const val _ID = "_id"
        const val _VERSION = "_version"

        const val _SEQ_NO = "_seq_no"
        const val _PRIMARY_TERM = "_primary_term"

        var supportedClusterMetricsSettings: SupportedClusterMetricsSettings? = null
    }
}

fun Monitor.isBucketLevelMonitor(): Boolean = this.monitorType == Monitor.MonitorType.BUCKET_LEVEL_MONITOR

fun XContentBuilder.optionalUserField(name: String, user: User?): XContentBuilder {
    if (user == null) {
        return nullField(name)
    }
    return this.field(name, user)
}

fun XContentBuilder.optionalTimeField(name: String, instant: Instant?): XContentBuilder {
    if (instant == null) {
        return nullField(name)
    }
    // second name as readableName should be different than first name
    return this.timeField(name, "${name}_in_millis", instant.toEpochMilli())
}

fun XContentParser.instant(): Instant? {
    return when {
        currentToken() == XContentParser.Token.VALUE_NULL -> null
        currentToken().isValue -> Instant.ofEpochMilli(longValue())
        else -> {
            XContentParserUtils.throwUnknownToken(currentToken(), tokenLocation)
            null // unreachable
        }
    }
}

/**
 * Extension function for ES 6.3 and above that duplicates the ES 6.2 XContentBuilder.string() method.
 */
fun XContentBuilder.string(): String = BytesReference.bytes(this).utf8ToString()
