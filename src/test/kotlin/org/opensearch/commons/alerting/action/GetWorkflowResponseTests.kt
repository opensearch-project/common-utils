package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.randomWorkflow
import org.opensearch.rest.RestStatus

class GetWorkflowResponseTests {

    @Test
    fun testGetWorkflowRequest() {
        val workflow = randomWorkflow()
        val response = GetWorkflowResponse(
            id = "id", version = 1, seqNo = 1, primaryTerm = 1, status = RestStatus.OK, workflow = workflow
        )
        val out = BytesStreamOutput()
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRes = GetWorkflowResponse(sin)
        Assertions.assertEquals("id", newRes.id)
        Assertions.assertEquals(workflow.name, newRes.workflow!!.name)
        Assertions.assertEquals(workflow.owner, newRes.workflow!!.owner)
    }
}
