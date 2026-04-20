/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.opensearch.commons.alerting.parser
import org.opensearch.commons.alerting.randomQueryLevelMonitor

class SchedulePayloadBuilderTests {

    @Test
    fun `buildTargetInput contains monitorConfig only`() {
        val monitor = randomQueryLevelMonitor()
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        assertFalse("monitorId should not be at top level", parsed.containsKey("monitorId"))
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
    fun `buildTargetInput includes metadata inside monitorConfig`() {
        val monitor = randomQueryLevelMonitor().copy(
            metadata = mapOf("appId" to "test-app", "workspaceId" to "ws-123", "ebCellAccountId" to "111222333444")
        )
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        // metadata should NOT be flattened at top level
        assertFalse(parsed.containsKey("appId"))
        assertFalse(parsed.containsKey("workspaceId"))
        assertFalse(parsed.containsKey("ebCellAccountId"))
        // They should be inside the serialized monitorConfig under metadata
        val monitorConfigJson = parsed["monitorConfig"] as String
        assertTrue(monitorConfigJson.contains("\"metadata\""))
        assertTrue(monitorConfigJson.contains("\"appId\":\"test-app\""))
        assertTrue(monitorConfigJson.contains("\"workspaceId\":\"ws-123\""))
        assertTrue(monitorConfigJson.contains("\"ebCellAccountId\":\"111222333444\""))
    }

    @Test
    fun `buildTargetInput without metadata has no extra fields`() {
        val monitor = randomQueryLevelMonitor()
        val result = SchedulePayloadBuilder.buildTargetInput(monitor = monitor)
        val parsed = parser(result).map()
        assertFalse(parsed.containsKey("appId"))
        assertFalse(parsed.containsKey("workspaceId"))
        assertFalse(parsed.containsKey("ebCellAccountId"))
    }
}
