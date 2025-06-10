package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.randomFinding
import org.opensearch.core.common.io.stream.StreamInput

class PublishFindingsRequestTests {

    @Test
    fun `test delete monitor request`() {
        val findings = listOf(randomFinding())
        val monitorId = "mid"
        val req = PublishFindingsRequest(monitorId, findings)
        assertNotNull(req)
        assertEquals(monitorId, req.monitorId)
        assertEquals(findings, req.findings)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = PublishFindingsRequest(sin)
        assertEquals(monitorId, newReq.monitorId)
        assertEquals(findings.size, newReq.findings.size)
        assertEquals(findings[0].id, newReq.findings[0].id)
    }
}
