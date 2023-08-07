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

        val finding = randomFinding()
        val monitorId = "mid"
        val req = PublishFindingsRequest(monitorId, finding)
        assertNotNull(req)
        assertEquals(monitorId, req.monitorId)
        assertEquals(finding, req.finding)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = PublishFindingsRequest(sin)
        assertEquals(monitorId, newReq.monitorId)
        assertEquals(finding.id, newReq.finding.id)
    }
}
