/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils

/**
 * Represents the payload for an externally scheduled monitor job.
 *
 * Written to the schedule target (e.g. SQS) by the monitor CRUD path,
 * and parsed back by the job poller at execution time.
 *
 * [jobStartTime] is a String to support both scheduler placeholders
 * (e.g. "<aws.scheduler.scheduled-time>") on the producer side and
 * actual ISO-8601 timestamps on the consumer side.
 */
data class ScheduleJobPayload(
    val monitorId: String,
    val jobStartTime: String,
    val monitorConfig: String
) : ToXContentObject {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field(MONITOR_ID_FIELD, monitorId)
        builder.field(JOB_START_TIME_FIELD, jobStartTime)
        builder.field(MONITOR_CONFIG_FIELD, monitorConfig)
        builder.endObject()
        return builder
    }

    /**
     * Deserializes [monitorConfig] into a [Monitor] using the provided registry.
     * The [monitorId] is passed through so the monitor retains its identity.
     */
    fun toMonitor(xContentRegistry: NamedXContentRegistry): Monitor {
        return XContentType.JSON.xContent()
            .createParser(xContentRegistry, LoggingDeprecationHandler.INSTANCE, monitorConfig)
            .use { parser ->
                parser.nextToken()
                Monitor.parse(parser, monitorId, Monitor.NO_VERSION)
            }
    }

    companion object {
        const val MONITOR_ID_FIELD = "monitorId"
        const val JOB_START_TIME_FIELD = "job_start_time"
        const val MONITOR_CONFIG_FIELD = "monitorConfig"

        fun parse(xcp: XContentParser): ScheduleJobPayload {
            var monitorId: String? = null
            var jobStartTime: String? = null
            var monitorConfig: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                xcp.currentToken(),
                xcp
            )
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    MONITOR_ID_FIELD -> monitorId = xcp.text()
                    JOB_START_TIME_FIELD -> jobStartTime = xcp.text()
                    MONITOR_CONFIG_FIELD -> monitorConfig = xcp.text()
                    else -> xcp.skipChildren()
                }
            }

            return ScheduleJobPayload(
                monitorId = requireNotNull(monitorId) { "Payload missing $MONITOR_ID_FIELD field" },
                jobStartTime = requireNotNull(jobStartTime) { "Payload missing $JOB_START_TIME_FIELD field" },
                monitorConfig = requireNotNull(monitorConfig) { "Payload missing $MONITOR_CONFIG_FIELD field" }
            )
        }
    }
}
