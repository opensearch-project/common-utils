package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.Table
import org.opensearch.core.common.io.stream.StreamInput
import java.time.Instant

internal class GetFindingsRequestTests {


    @Test
    fun `test get findings request`() {
        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetFindingsRequest("2121", table, "1", "finding_index_name", listOf("1", "2"), "severity", "detectionType", listOf("id1", "id2", ), Instant.now(), Instant.now().plusSeconds(30000))
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetFindingsRequest(sin)

        assertEquals("1", newReq.monitorId)
        assertEquals("2121", newReq.findingId)
        assertEquals("finding_index_name", newReq.findingIndex)
        assertEquals("severity", newReq.severity)
        assertEquals("detectionType", newReq.detectionType)
        assertEquals(table, newReq.table)
        assertTrue(newReq.monitorIds!!.contains("1"))
        assertTrue(newReq.monitorIds!!.contains("2"))
        assertTrue(newReq.findingIds!!.contains("id1"))
        assertTrue(newReq.findingIds!!.contains("id2"))
        assertTrue(newReq.startTime!! < newReq.endTime, "startTime less than endTime")
    }

    @Test
    fun `test validate returns null`() {
        val table = Table("asc", "sortString", null, 1, 0, "")

        val req = GetFindingsRequest("2121", table, "1", "active", listOf("1", "2"), "severity", "detectionType", listOf("id1", "id2", ), Instant.now(), Instant.now().plusSeconds(30000))
        assertNotNull(req)
        assertNull(req.validate())
    }
}
