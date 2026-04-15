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

class MonitorPayloadBuilderTests {

    @Test
    fun `buildTargetInput contains all routing fields`() {
        val monitor = randomQueryLevelMonitor()
        val result = MonitorPayloadBuilder.buildTargetInput(
            monitor = monitor,
            appId = "app-1",
            tenantId = "tenant-1",
            workspaceId = "ws-1",
            collectionEndpoint = "https://endpoint.example.com"
        )
        val parsed = parser(result).map()
        assertTrue(parsed.containsKey("appId"))
        assertTrue(parsed.containsKey("tenantId"))
        assertTrue(parsed.containsKey("monitorId"))
        assertTrue(parsed.containsKey("workspaceId"))
        assertTrue(parsed.containsKey("collectionEndpoint"))
        assertTrue(parsed.containsKey("monitorConfig"))
        assertFalse("job_start_time should be absent when placeholder is empty", parsed.containsKey("job_start_time"))
    }

    @Test
    fun `buildTargetInput includes job_start_time when placeholder provided`() {
        val monitor = randomQueryLevelMonitor()
        val result = MonitorPayloadBuilder.buildTargetInput(
            monitor = monitor,
            appId = "app-1",
            tenantId = "tenant-1",
            workspaceId = "ws-1",
            collectionEndpoint = "https://endpoint.example.com",
            jobStartTimePlaceholder = "<scheduler.scheduled-time>"
        )
        val parsed = parser(result).map()
        assertTrue(parsed.containsKey("job_start_time"))
        assertTrue(parsed["job_start_time"] == "<scheduler.scheduled-time>")
    }
}
