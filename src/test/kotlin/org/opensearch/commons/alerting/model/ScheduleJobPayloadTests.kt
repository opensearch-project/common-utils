/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.parser
import org.opensearch.commons.alerting.randomQueryLevelMonitor
import org.opensearch.commons.alerting.xContentRegistry
import org.opensearch.core.xcontent.ToXContent

class ScheduleJobPayloadTests {

    private fun serializeMonitor(monitor: Monitor): String {
        val builder = org.opensearch.common.xcontent.XContentFactory.jsonBuilder()
        monitor.toXContentWithUser(builder, ToXContent.EMPTY_PARAMS)
        return builder.toString()
    }

    @Test
    fun `round-trip with monitorId and jobStartTime`() {
        val monitor = randomQueryLevelMonitor().copy(id = "mon-123")
        val payload = ScheduleJobPayload(
            monitorId = monitor.id,
            jobStartTime = "2026-04-23T10:00:00Z",
            monitorConfig = serializeMonitor(monitor)
        )

        val builder = org.opensearch.common.xcontent.XContentFactory.jsonBuilder()
        payload.toXContent(builder, ToXContent.EMPTY_PARAMS)
        val json = builder.toString()

        val parsed = ScheduleJobPayload.parse(parser(json))
        assertEquals("mon-123", parsed.monitorId)
        assertEquals("2026-04-23T10:00:00Z", parsed.jobStartTime)
        assertEquals(payload.monitorConfig, parsed.monitorConfig)
    }

    @Test
    fun `round-trip with EB placeholder as jobStartTime`() {
        val monitor = randomQueryLevelMonitor().copy(id = "mon-eb")
        val payload = ScheduleJobPayload(
            monitorId = monitor.id,
            jobStartTime = "<aws.scheduler.scheduled-time>",
            monitorConfig = serializeMonitor(monitor)
        )

        val builder = org.opensearch.common.xcontent.XContentFactory.jsonBuilder()
        payload.toXContent(builder, ToXContent.EMPTY_PARAMS)
        val json = builder.toString()

        val parsed = ScheduleJobPayload.parse(parser(json))
        assertEquals("<aws.scheduler.scheduled-time>", parsed.jobStartTime)
    }

    @Test
    fun `toMonitor preserves id and fields`() {
        val monitor = randomQueryLevelMonitor().copy(id = "mon-456")
        val payload = ScheduleJobPayload(
            monitorId = monitor.id,
            jobStartTime = "2026-04-23T10:00:00Z",
            monitorConfig = serializeMonitor(monitor)
        )

        val restored = payload.toMonitor(xContentRegistry())
        assertEquals("mon-456", restored.id)
        assertEquals(monitor.name, restored.name)
        assertEquals(monitor.monitorType, restored.monitorType)
    }

    @Test
    fun `toMonitor preserves metadata`() {
        val monitor = randomQueryLevelMonitor().copy(
            id = "mon-meta",
            metadata = mapOf("appId" to "app1", "tenantId" to "t1")
        )
        val payload = ScheduleJobPayload(
            monitorId = monitor.id,
            jobStartTime = "2026-04-23T10:00:00Z",
            monitorConfig = serializeMonitor(monitor)
        )

        val restored = payload.toMonitor(xContentRegistry())
        assertEquals(
            mapOf("appId" to "app1", "tenantId" to "t1"),
            restored.metadata
        )
    }

    @Test
    fun `parse throws on missing monitorId`() {
        val monitor = randomQueryLevelMonitor()
        val json = "{\"job_start_time\":\"2026-04-23T10:00:00Z\"," +
            "\"monitorConfig\":\"${serializeMonitor(monitor).replace("\"", "\\\"")}\"}"

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ScheduleJobPayload.parse(parser(json))
        }
    }

    @Test
    fun `parse throws on missing jobStartTime`() {
        val monitor = randomQueryLevelMonitor()
        val json = "{\"monitorId\":\"mon-123\"," +
            "\"monitorConfig\":\"${serializeMonitor(monitor).replace("\"", "\\\"")}\"}"

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ScheduleJobPayload.parse(parser(json))
        }
    }
}
