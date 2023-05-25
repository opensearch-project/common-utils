package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.rest.RestRequest

class GetWorkflowRequestTests {

    @Test
    fun testGetWorkflowRequest() {
        val request = GetWorkflowRequest("w1", RestRequest.Method.GET)
        Assertions.assertNull(request.validate())

        val out = BytesStreamOutput()
        request.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetWorkflowRequest(sin)
        Assertions.assertEquals("w1", newReq.workflowId)
        Assertions.assertEquals(RestRequest.Method.GET, newReq.method)
    }
}
