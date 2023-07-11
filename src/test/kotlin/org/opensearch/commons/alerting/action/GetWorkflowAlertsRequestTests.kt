package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.Table

internal class GetWorkflowAlertsRequestTests {

    @Test
    fun `test get alerts request`() {

        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetWorkflowAlertsRequest(
            table = table,
            severityLevel = "1",
            alertState = "active",
            getAssociatedAlerts = true,
            workflowIds = listOf("w1", "w2"),
            alertIds = emptyList(),
            alertIndex = null,
            associatedAlertsIndex = null,
            monitorIds = emptyList()
        )
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetWorkflowAlertsRequest(sin)

        assertEquals("1", newReq.severityLevel)
        assertEquals("active", newReq.alertState)
        assertEquals(table, newReq.table)
        assertTrue(newReq.workflowIds!!.contains("w1"))
        assertTrue(newReq.workflowIds!!.contains("w2"))
        assertTrue(newReq.alertIds!!.isEmpty())
        assertTrue(newReq.monitorIds!!.isEmpty())
        assertNull(newReq.alertIndex)
        assertNull(newReq.associatedAlertsIndex)
        assertTrue(newReq.getAssociatedAlerts)
    }

    @Test
    fun `test get alerts request with custom alerts and associated alerts indices`() {

        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetWorkflowAlertsRequest(
            table = table,
            severityLevel = "1",
            alertState = "active",
            getAssociatedAlerts = true,
            workflowIds = listOf("w1", "w2"),
            alertIds = emptyList(),
            alertIndex = "alertIndex",
            associatedAlertsIndex = "associatedAlertsIndex",
            monitorIds = emptyList()
        )
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetWorkflowAlertsRequest(sin)

        assertEquals("1", newReq.severityLevel)
        assertEquals("active", newReq.alertState)
        assertEquals(table, newReq.table)
        assertTrue(newReq.workflowIds!!.contains("w1"))
        assertTrue(newReq.workflowIds!!.contains("w2"))
        assertTrue(newReq.alertIds!!.isEmpty())
        assertTrue(newReq.monitorIds!!.isEmpty())
        assertEquals(newReq.alertIndex, "alertIndex")
        assertEquals(newReq.associatedAlertsIndex, "associatedAlertsIndex")
        assertTrue(newReq.getAssociatedAlerts)
    }

    @Test
    fun `test validate returns null`() {
        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetWorkflowAlertsRequest(
            table = table,
            severityLevel = "1",
            alertState = "active",
            getAssociatedAlerts = true,
            workflowIds = listOf("w1, w2"),
            alertIds = emptyList(),
            alertIndex = null,
            associatedAlertsIndex = null
        )
        assertNotNull(req)
        assertNull(req.validate())
    }
}
