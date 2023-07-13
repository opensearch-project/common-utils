package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.commons.alerting.builder
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.randomAlert
import org.opensearch.commons.alerting.randomChainedAlert
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.util.string
import org.opensearch.core.xcontent.ToXContent
import java.time.Instant
import java.util.Collections

class GetWorkflowAlertsResponseTests {

    @Test
    fun `test get alerts response with no alerts`() {
        val req = GetWorkflowAlertsResponse(Collections.emptyList(), emptyList(), 0)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetWorkflowAlertsResponse(sin)
        assertTrue(newReq.alerts.isEmpty())
        assertTrue(newReq.associatedAlerts.isEmpty())
        assertEquals(0, newReq.totalAlerts)
    }

    @Test
    fun `test get alerts response with alerts`() {
        val chainedAlert1 = randomChainedAlert()
        val chainedAlert2 = randomChainedAlert()
        val alert1 = randomAlert()
        val alert2 = randomAlert()
        val req = GetWorkflowAlertsResponse(listOf(chainedAlert1, chainedAlert2), listOf(alert1, alert2), 2)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetWorkflowAlertsResponse(sin)
        assertEquals(2, newReq.alerts.size)
        assertEquals(2, newReq.associatedAlerts.size)
        assertEquals(2, newReq.totalAlerts)
        assertTrue(newReq.alerts.contains(chainedAlert1))
        assertTrue(newReq.alerts.contains(chainedAlert2))
        assertTrue(newReq.associatedAlerts.contains(alert1))
        assertTrue(newReq.associatedAlerts.contains(alert2))
    }

    @Test
    fun `test toXContent for get alerts response`() {
        val alert = Alert(
            monitorId = "id",
            monitorName = "name",
            monitorVersion = Alert.NO_VERSION,
            monitorUser = randomUser(),
            triggerId = "triggerId",
            triggerName = "triggerNamer",
            state = Alert.State.ACKNOWLEDGED,
            startTime = Instant.ofEpochMilli(1688591410974),
            lastNotificationTime = null,
            errorMessage = null,
            errorHistory = emptyList(),
            severity = "high",
            actionExecutionResults = emptyList(),
            schemaVersion = 0,
            aggregationResultBucket = null,
            findingIds = emptyList(),
            relatedDocIds = emptyList(),
            executionId = "executionId",
            workflowId = "wid",
            workflowName = "",
            associatedAlertIds = emptyList()
        )

        val req = GetWorkflowAlertsResponse(listOf(alert), emptyList(), 1)
        var actualXContentString = req.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val expectedXContentString =
            "{\"alerts\":[{\"id\":\"\",\"version\":-1,\"monitor_id\":\"id\"," +
                "\"workflow_id\":\"wid\",\"workflow_name\":\"\",\"associated_alert_ids\":[]," +
                "\"schema_version\":0,\"monitor_version\":-1,\"monitor_name\":\"name\",\"execution_id\":" +
                "\"executionId\",\"trigger_id\":\"triggerId\",\"trigger_name\":\"triggerNamer\",\"finding_ids\":[]," +
                "\"related_doc_ids\":[],\"state\":\"ACKNOWLEDGED\",\"error_message\":null,\"alert_history\":[]," +
                "\"severity\":\"high\",\"action_execution_results\":[],\"start_time\":1688591410974," +
                "\"last_notification_time\":null,\"end_time\":null,\"acknowledged_time\":null}]," +
                "\"associatedAlerts\":[],\"totalAlerts\":1}"
        assertEquals(expectedXContentString, actualXContentString)
    }
}
