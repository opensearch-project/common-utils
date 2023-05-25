package org.opensearch.commons.alerting.action

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput

class DeleteWorkflowRequestTests {

    @Test
    fun `test delete workflow request`() {

        val req = DeleteWorkflowRequest("1234", true)
        Assert.assertNotNull(req)
        Assert.assertEquals("1234", req.workflowId)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = DeleteWorkflowRequest(sin)
        Assert.assertEquals("1234", newReq.workflowId)
    }
}
