package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.commons.alerting.builder
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.util.string
import org.opensearch.core.xcontent.ToXContent
import java.time.Instant
import java.util.Collections

class GetAlertsResponseTests {

    @Test
    fun `test get alerts response with no alerts`() {
        val req = GetAlertsResponse(Collections.emptyList(), 0)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetAlertsResponse(sin)
        Assertions.assertTrue(newReq.alerts.isEmpty())
        assertEquals(0, newReq.totalAlerts)
    }

    @Test
    fun `test get alerts response with alerts`() {
        val alert = Alert(
            monitorId = "id",
            monitorName = "name",
            monitorVersion = Alert.NO_VERSION,
            monitorUser = randomUser(),
            triggerId = "triggerId",
            triggerName = "triggerNamer",
            state = Alert.State.ACKNOWLEDGED,
            startTime = Instant.now(),
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
            workflowId = "workflowId",
            workflowName = "",
            associatedAlertIds = emptyList()
        )
        val req = GetAlertsResponse(listOf(alert), 1)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetAlertsResponse(sin)
        assertEquals(1, newReq.alerts.size)
        assertEquals(alert, newReq.alerts[0])
        assertEquals(1, newReq.totalAlerts)
        assertEquals(newReq.alerts[0].workflowId, "workflowId")
    }

    @Test
    fun `test toXContent for get alerts response`() {
        val now = Instant.now()
        val alert = Alert(
            monitorId = "id",
            monitorName = "name",
            monitorVersion = Alert.NO_VERSION,
            monitorUser = randomUser(),
            triggerId = "triggerId",
            triggerName = "triggerNamer",
            state = Alert.State.ACKNOWLEDGED,
            startTime = now,
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

        val req = GetAlertsResponse(listOf(alert), 1)
        var actualXContentString = req.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val expectedXContentString = "{\"alerts\":[{\"id\":\"\",\"version\":-1,\"monitor_id\":\"id\",\"workflow_id\":\"wid\"," +
            "\"workflow_name\":\"\",\"associated_alert_ids\":[],\"schema_version\":0,\"monitor_version\":-1," +
            "\"monitor_name\":\"name\",\"execution_id\":\"executionId\",\"trigger_id\":\"triggerId\"," +
            "\"trigger_name\":\"triggerNamer\",\"finding_ids\":[],\"related_doc_ids\":[],\"state\":\"ACKNOWLEDGED\"," +
            "\"error_message\":null,\"alert_history\":[],\"severity\":\"high\",\"action_execution_results\":[]," +
            "\"start_time\":${now.toEpochMilli()},\"last_notification_time\":null,\"end_time\":null," +
            "\"acknowledged_time\":null}],\"totalAlerts\":1}"
        assertEquals(expectedXContentString, actualXContentString)
    }
}
