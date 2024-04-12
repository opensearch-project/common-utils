package org.opensearch.commons.alerting.util

import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.settings.SupportedClusterMetricsSettings
import org.opensearch.commons.authuser.User
import org.opensearch.core.common.bytes.BytesReference
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.time.Instant

class IndexUtils {
    companion object {
        /**
         * This regex asserts that the string:
         *  The index does not start with an underscore _, hyphen -, or plus sign +
         *  The index does not contain two consecutive periods (e.g., `..`)
         *  The index does not contain any whitespace characters, commas, backslashes, forward slashes, asterisks,
         *   question marks, double quotes, less than or greater than signs, pipes, colons, or periods.
         *  The length of the index must be between 1 and 255 characters
         */
        val VALID_INDEX_NAME_REGEX = Regex("""^(?![_\-\+])(?!.*\.\.)[^\s,\\\/\*\?"<>|#:\.]{1,255}$""")

        /**
         * This regex asserts that the string:
         *  The index pattern can start with an optional period
         *  The index pattern can contain lowercase letters, digits, underscores, hyphens, asterisks, and periods
         *  The length of the index pattern must be between 1 and 255 characters
         */
        val INDEX_PATTERN_REGEX = Regex("""^(?=.{1,255}$)\.?[a-z0-9_\-\*\.]+$""")

        /**
         * This regex asserts that the string:
         *  Starts with a lowercase letter, digit, or asterisk
         *  Contains a sequence of characters followed by an optional colon and another sequence of characters
         *  The sequences of characters can include lowercase letters, uppercase letters, digits, underscores, asterisks, or hyphens
         *  The total length of the string can range from 1 to 255 characters
         */
        val CLUSTER_PATTERN_REGEX = Regex("^(?=.{1,255}$)[a-z0-9*]([a-zA-Z0-9_*-]*:?[a-zA-Z0-9_*-]*)$")

        const val NO_SCHEMA_VERSION = 0

        const val MONITOR_MAX_INPUTS = 1
        const val WORKFLOW_MAX_INPUTS = 1

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
