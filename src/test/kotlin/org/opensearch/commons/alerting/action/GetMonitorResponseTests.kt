/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.CronSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.randomUser
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.test.OpenSearchTestCase
import java.time.Instant
import java.time.ZoneId

class GetMonitorResponseTests : OpenSearchTestCase() {

    fun `test get monitor response`() {
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, null, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetMonitorResponse(sin)
        assertEquals("1234", newReq.id)
        assertEquals(1L, newReq.version)
        assertEquals(null, newReq.monitor)
    }

    fun `test get monitor response with monitor`() {
        val cronExpression = "31 * * * *" // Run at minute 31.
        val testInstance = Instant.ofEpochSecond(1538164858L)

        val cronSchedule = CronSchedule(cronExpression, ZoneId.of("Asia/Kolkata"), testInstance)
        val monitor = Monitor(
            id = "123",
            version = 0L,
            name = "test-monitor",
            enabled = true,
            schedule = cronSchedule,
            lastUpdateTime = Instant.now(),
            enabledTime = Instant.now(),
            monitorType = Monitor.MonitorType.QUERY_LEVEL_MONITOR.value,
            user = randomUser(),
            schemaVersion = 0,
            inputs = mutableListOf(),
            triggers = mutableListOf(),
            uiMetadata = mutableMapOf()
        )
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, monitor, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetMonitorResponse(sin)
        assertEquals("1234", newReq.id)
        assertEquals(1L, newReq.version)
        assertNotNull(newReq.monitor)
    }
}
