/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.core.xcontent.ToXContent

/**
 * Builds the message payload for external schedule targets (e.g. SQS).
 *
 * The payload contains the monitor ID and full monitor configuration.
 * Routing information (appId, tenantId, etc.) is part of the monitor
 * definition and extracted during execution.
 */
object SchedulePayloadBuilder {

    /**
     * @param monitor the monitor to serialize into the payload
     * @param jobStartTimePlaceholder scheduler-specific placeholder for execution time
     *        (e.g. a context variable that the scheduler replaces at invocation time)
     */
    fun buildTargetInput(
        monitor: Monitor,
        jobStartTimePlaceholder: String = ""
    ): String {
        val monitorConfigJson = serializeMonitorConfig(monitor)

        val builder = XContentFactory.jsonBuilder()
        builder.startObject()
        if (jobStartTimePlaceholder.isNotEmpty()) {
            builder.field("job_start_time", jobStartTimePlaceholder)
        }
        builder.field("monitorId", monitor.id)
        builder.field("monitorConfig", monitorConfigJson)
        builder.endObject()
        return builder.toString()
    }

    private fun serializeMonitorConfig(monitor: Monitor): String {
        val builder = XContentFactory.jsonBuilder()
        monitor.toXContent(builder, ToXContent.EMPTY_PARAMS)
        return builder.toString()
    }
}
