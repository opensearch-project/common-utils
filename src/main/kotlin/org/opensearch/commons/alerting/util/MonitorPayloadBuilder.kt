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
 * Top-level fields for routing, nested monitorConfig with full monitor JSON.
 */
object MonitorPayloadBuilder {

    /**
     * @param jobStartTimePlaceholder scheduler-specific placeholder for execution time
     *        (e.g. a context variable that the scheduler replaces at invocation time)
     */
    fun buildTargetInput(
        monitor: Monitor,
        appId: String,
        tenantId: String,
        workspaceId: String,
        collectionEndpoint: String,
        jobStartTimePlaceholder: String = ""
    ): String {
        val monitorConfigJson = serializeMonitorConfig(monitor)

        val builder = XContentFactory.jsonBuilder()
        builder.startObject()
        if (jobStartTimePlaceholder.isNotEmpty()) {
            builder.field("job_start_time", jobStartTimePlaceholder)
        }
        builder.field("appId", appId)
        builder.field("tenantId", tenantId)
        builder.field("monitorId", monitor.id)
        builder.field("workspaceId", workspaceId)
        builder.field("collectionEndpoint", collectionEndpoint)
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
