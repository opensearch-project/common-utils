package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.CronSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.randomUser
import java.time.Instant
import java.time.ZoneId

class IndexMonitorResponseTests {

    @Test
    fun `test index monitor response with monitor`() {
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
            monitorType = Monitor.MonitorType.QUERY_LEVEL_MONITOR,
            user = randomUser(),
            schemaVersion = 0,
            inputs = mutableListOf(),
            triggers = mutableListOf(),
            uiMetadata = mutableMapOf()
        )
        val req = IndexMonitorResponse("1234", 1L, 2L, 0L, monitor)
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexMonitorResponse(sin)
        Assertions.assertEquals("1234", newReq.id)
        Assertions.assertEquals(1L, newReq.version)
        Assertions.assertNotNull(newReq.monitor)
    }
}
