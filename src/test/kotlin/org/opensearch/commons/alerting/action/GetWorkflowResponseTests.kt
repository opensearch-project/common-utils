package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.CompositeInput
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.commons.alerting.randomDelegate
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.randomWorkflow
import org.opensearch.core.rest.RestStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetWorkflowResponseTests {

    @Test
    fun testGetWorkflowResponse() {
        val workflow = randomWorkflow(auditDelegateMonitorAlerts = false)
        val response = GetWorkflowResponse(
            id = "id", version = 1, seqNo = 1, primaryTerm = 1, status = RestStatus.OK, workflow = workflow
        )
        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRes = GetWorkflowResponse(sin)
        Assertions.assertEquals("id", newRes.id)
        Assertions.assertFalse(newRes.workflow!!.auditDelegateMonitorAlerts!!)
        Assertions.assertEquals(workflow.name, newRes.workflow!!.name)
        Assertions.assertEquals(workflow.owner, newRes.workflow!!.owner)
    }

    @Test
    fun testGetWorkflowResponseWhereAuditDelegateMonitorAlertsFlagIsNotSet() {
        val workflow = Workflow(
            id = "",
            version = Workflow.NO_VERSION,
            name = "test",
            enabled = true,
            schemaVersion = 2,
            schedule = IntervalSchedule(1, ChronoUnit.MINUTES),
            lastUpdateTime = Instant.now(),
            enabledTime = Instant.now(),
            workflowType = Workflow.WorkflowType.COMPOSITE,
            user = randomUser(),
            inputs = listOf(CompositeInput(org.opensearch.commons.alerting.model.Sequence(listOf(randomDelegate())))),
            owner = "",
            triggers = listOf()
        )
        val response = GetWorkflowResponse(
            id = "id", version = 1, seqNo = 1, primaryTerm = 1, status = RestStatus.OK, workflow = workflow
        )
        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRes = GetWorkflowResponse(sin)
        Assertions.assertEquals("id", newRes.id)
        Assertions.assertTrue(newRes.workflow!!.auditDelegateMonitorAlerts!!)
        Assertions.assertEquals(workflow.name, newRes.workflow!!.name)
        Assertions.assertEquals(workflow.owner, newRes.workflow!!.owner)
        Assertions.assertEquals(workflow.auditDelegateMonitorAlerts, newRes.workflow!!.auditDelegateMonitorAlerts)
    }
}
