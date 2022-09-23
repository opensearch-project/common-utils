package org.opensearch.commons.alerting.action

import org.junit.Assert
import org.junit.Test
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput

class DeleteMonitorRequestTests {

    @Test
    fun `test delete monitor request`() {

        val req = DeleteMonitorRequest("1234", WriteRequest.RefreshPolicy.IMMEDIATE)
        Assert.assertNotNull(req)
        Assert.assertEquals("1234", req.monitorId)
        Assert.assertEquals("true", req.refreshPolicy.value)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = DeleteMonitorRequest(sin)
        Assert.assertEquals("1234", newReq.monitorId)
        Assert.assertEquals("true", newReq.refreshPolicy.value)
    }
}
