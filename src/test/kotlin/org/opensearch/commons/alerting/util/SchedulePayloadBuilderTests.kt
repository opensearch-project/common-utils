/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.opensearch.commons.alerting.parser
import org.opensearch.commons.alerting.randomQueryLevelMonitor

class SchedulePayloadBuilderTests {

    @Test
    fun `buildTargetInput contains monitorId and monitorConfig`() {
        val monitor = randomQueryLevelMonitor()
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        assertTrue(parsed.containsKey("monitorId"))
        assertTrue(parsed.containsKey("monitorConfig"))
        assertFalse("job_start_time should be absent when placeholder is empty", parsed.containsKey("job_start_time"))
    }

    @Test
    fun `buildTargetInput includes job_start_time when placeholder provided`() {
        val monitor = randomQueryLevelMonitor()
        val result = SchedulePayloadBuilder.buildTargetInput(
            monitor = monitor,
            jobStartTimePlaceholder = "<scheduler.scheduled-time>"
        )
        val parsed = parser(result).map()
        assertTrue(parsed.containsKey("job_start_time"))
        assertTrue(parsed["job_start_time"] == "<scheduler.scheduled-time>")
    }

    @Test
    fun `buildTargetInput includes additionalFields as top-level fields`() {
        val monitor = randomQueryLevelMonitor().copy(
            additionalFields = mapOf("appId" to "test-app", "workspaceId" to "ws-123", "ebCellAccountId" to "111222333444")
        )
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        assertEquals("test-app", parsed["appId"])
        assertEquals("ws-123", parsed["workspaceId"])
        assertEquals("111222333444", parsed["ebCellAccountId"])
    }

    @Test
    fun `buildTargetInput without additionalFields has no extra fields`() {
        val monitor = randomQueryLevelMonitor()
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        assertFalse(parsed.containsKey("appId"))
        assertFalse(parsed.containsKey("workspaceId"))
        assertFalse(parsed.containsKey("ebCellAccountId"))
    }
}
