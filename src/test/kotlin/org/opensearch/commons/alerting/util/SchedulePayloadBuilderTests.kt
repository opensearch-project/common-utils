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
}
