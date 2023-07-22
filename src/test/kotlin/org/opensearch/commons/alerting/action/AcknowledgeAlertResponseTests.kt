package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.alerts.AlertError
import org.opensearch.commons.alerting.model.ActionExecutionResult
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.randomUser
import org.opensearch.core.common.io.stream.StreamInput
import java.time.Instant

class AcknowledgeAlertResponseTests {

    @Test
    fun `test acknowledge alert response`() {

        val acknowledged = mutableListOf(
            Alert(
                id = "1234",
                version = 0L,
                schemaVersion = 1,
                monitorId = "monitor-1234",
                workflowId = "",
                workflowName = "",
                monitorName = "test-monitor",
                monitorVersion = 0L,
                monitorUser = randomUser(),
                triggerId = "trigger-14",
                triggerName = "test-trigger",
                findingIds = ArrayList(),
                relatedDocIds = ArrayList(),
                state = Alert.State.ACKNOWLEDGED,
                startTime = Instant.now(),
                endTime = Instant.now(),
                lastNotificationTime = Instant.now(),
                acknowledgedTime = Instant.now(),
                errorMessage = null,
                errorHistory = ArrayList(),
                severity = "sev-2",
                actionExecutionResults = ArrayList(),
                aggregationResultBucket = null,
                executionId = null,
                associatedAlertIds = emptyList()
            )
        )
        val failed = mutableListOf(
            Alert(
                id = "1234",
                version = 0L,
                schemaVersion = 1,
                monitorId = "monitor-1234",
                workflowId = "",
                workflowName = "",
                monitorName = "test-monitor",
                monitorVersion = 0L,
                monitorUser = randomUser(),
                triggerId = "trigger-14",
                triggerName = "test-trigger",
                findingIds = ArrayList(),
                relatedDocIds = ArrayList(),
                state = Alert.State.ERROR,
                startTime = Instant.now(),
                endTime = Instant.now(),
                lastNotificationTime = Instant.now(),
                acknowledgedTime = Instant.now(),
                errorMessage = null,
                errorHistory = mutableListOf(AlertError(Instant.now(), "Error msg")),
                severity = "sev-2",
                actionExecutionResults = mutableListOf(ActionExecutionResult("7890", null, 0)),
                aggregationResultBucket = null,
                executionId = null,
                associatedAlertIds = emptyList()
            )
        )
        val missing = mutableListOf("1", "2", "3", "4")

        val req = AcknowledgeAlertResponse(acknowledged, failed, missing)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = AcknowledgeAlertResponse(sin)
        assertEquals(1, newReq.acknowledged.size)
        assertEquals(1, newReq.failed.size)
        assertEquals(4, newReq.missing.size)
    }
}
