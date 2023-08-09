package org.opensearch.commons.alerting.action

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput

class DeleteWorkflowResponseTests {

    @Test
    fun `test delete workflow response`() {

        val res = DeleteWorkflowResponse(id = "w1", version = 1, nonDeletedMonitors = listOf("m1"))
        Assert.assertNotNull(res)
        Assert.assertEquals("w1", res.id)

        val out = BytesStreamOutput()
        res.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRes = DeleteWorkflowResponse(sin)
        Assert.assertEquals("w1", newRes.id)
        Assert.assertEquals("m1", newRes.nonDeletedMonitors!!.get(0))
        Assert.assertEquals(1, newRes.version)
    }
}
