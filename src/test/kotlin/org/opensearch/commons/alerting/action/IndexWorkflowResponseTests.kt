package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.CronSchedule
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.commons.alerting.randomUser
import java.time.Instant
import java.time.ZoneId

class IndexWorkflowResponseTests {

    @Test
    fun `test index workflow response with workflow`() {
        val cronExpression = "31 * * * *" // Run at minute 31.
        val testInstance = Instant.ofEpochSecond(1538164858L)

        val cronSchedule = CronSchedule(cronExpression, ZoneId.of("Asia/Kolkata"), testInstance)
        val workflow = Workflow(
            id = "123",
            version = 0L,
            name = "test-workflow",
            enabled = true,
            schedule = cronSchedule,
            lastUpdateTime = Instant.now(),
            enabledTime = Instant.now(),
            workflowType = Workflow.WorkflowType.COMPOSITE,
            user = randomUser(),
            schemaVersion = 0,
            inputs = mutableListOf(),
        )
        val req = IndexWorkflowResponse("1234", 1L, 2L, 0L, workflow)
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexWorkflowResponse(sin)
        Assertions.assertEquals("1234", newReq.id)
        Assertions.assertEquals(1L, newReq.version)
        Assertions.assertNotNull(newReq.workflow)
    }
}
