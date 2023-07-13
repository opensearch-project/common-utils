package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput

class DeleteMonitorRequestTests {

    @Test
    fun `test delete monitor request`() {

        val req = DeleteMonitorRequest("1234", WriteRequest.RefreshPolicy.IMMEDIATE)
        assertNotNull(req)
        assertEquals("1234", req.monitorId)
        assertEquals("true", req.refreshPolicy.value)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = DeleteMonitorRequest(sin)
        assertEquals("1234", newReq.monitorId)
        assertEquals("true", newReq.refreshPolicy.value)
    }
}
