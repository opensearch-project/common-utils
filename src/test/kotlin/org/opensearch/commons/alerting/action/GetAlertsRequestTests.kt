package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.Table

internal class GetAlertsRequestTests {

    @Test
    fun `test get alerts request`() {

        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetAlertsRequest(table, "1", "active", null, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetAlertsRequest(sin)

        assertEquals("1", newReq.severityLevel)
        assertEquals("active", newReq.alertState)
        assertNull(newReq.monitorId)
        assertEquals(table, newReq.table)
    }

    @Test
    fun `test get alerts request with filter`() {

        val table = Table("asc", "sortString", null, 1, 0, "")
        val req = GetAlertsRequest(table, "1", "active", null, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetAlertsRequest(sin)

        assertEquals("1", newReq.severityLevel)
        assertEquals("active", newReq.alertState)
        assertNull(newReq.monitorId)
        assertEquals(table, newReq.table)
    }

    @Test
    fun `test validate returns null`() {
        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetAlertsRequest(table, "1", "active", null, null)
        assertNotNull(req)
        assertNull(req.validate())
    }
}
