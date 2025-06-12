package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.randomFinding
import org.opensearch.core.common.io.stream.StreamInput

class PublishBatchFindingsRequestTests {

    @Test
    fun `test publish batch findings request`() {
        val findings = listOf(randomFinding(), randomFinding())
        val monitorId = "mid"
        val req = PublishBatchFindingsRequest(monitorId, findings)
        assertNotNull(req)
        assertEquals(monitorId, req.monitorId)
        assertEquals(findings, req.findings)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = PublishBatchFindingsRequest(sin)
        assertEquals(monitorId, newReq.monitorId)
        assertEquals(findings.size, newReq.findings.size)
        assert(newReq.findings.zip(findings).all { (f1, f2) -> f1.id == f2.id })
    }
}
