package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.NamedWriteableAwareStreamInput
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.settings.Settings
import org.opensearch.commons.alerting.randomCompositeWorkflow
import org.opensearch.commons.utils.recreateObject
import org.opensearch.rest.RestRequest
import org.opensearch.search.SearchModule

class IndexWorkflowRequestTests {

    @Test
    fun `test index workflow post request`() {

        val req = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.POST,
            randomCompositeWorkflow()
        )
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexWorkflowRequest(sin)
        Assertions.assertEquals("1234", newReq.workflowId)
        Assertions.assertEquals(1L, newReq.seqNo)
        Assertions.assertEquals(2L, newReq.primaryTerm)
        Assertions.assertEquals(RestRequest.Method.POST, newReq.method)
        Assertions.assertNotNull(newReq.workflow)
    }

    @Test
    fun `test index composite workflow post request`() {
        val req = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.POST,
            randomCompositeWorkflow()
        )
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val namedWriteableRegistry = NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)
        val newReq = IndexWorkflowRequest(NamedWriteableAwareStreamInput(sin, namedWriteableRegistry))
        Assertions.assertEquals("1234", newReq.workflowId)
        Assertions.assertEquals(1L, newReq.seqNo)
        Assertions.assertEquals(2L, newReq.primaryTerm)
        Assertions.assertEquals(RestRequest.Method.POST, newReq.method)
        Assertions.assertNotNull(newReq.workflow)
    }

    @Test
    fun `Index composite workflow serialize and deserialize transport object should be equal`() {
        val compositeWorkflowRequest = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.POST,
            randomCompositeWorkflow()
        )

        val recreatedObject = recreateObject(
            compositeWorkflowRequest,
            NamedWriteableRegistry(SearchModule(Settings.EMPTY, emptyList()).namedWriteables)
        ) { IndexWorkflowRequest(it) }
        Assertions.assertEquals(compositeWorkflowRequest.workflowId, recreatedObject.workflowId)
        Assertions.assertEquals(compositeWorkflowRequest.seqNo, recreatedObject.seqNo)
        Assertions.assertEquals(compositeWorkflowRequest.primaryTerm, recreatedObject.primaryTerm)
        Assertions.assertEquals(compositeWorkflowRequest.method, recreatedObject.method)
        Assertions.assertNotNull(recreatedObject.workflow)
        Assertions.assertEquals(compositeWorkflowRequest.workflow, recreatedObject.workflow)
    }

    @Test
    fun `test index workflow put request`() {

        val req = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomCompositeWorkflow()
        )
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexWorkflowRequest(sin)
        Assertions.assertEquals("1234", newReq.workflowId)
        Assertions.assertEquals(1L, newReq.seqNo)
        Assertions.assertEquals(2L, newReq.primaryTerm)
        Assertions.assertEquals(RestRequest.Method.PUT, newReq.method)
        Assertions.assertNotNull(newReq.workflow)
    }
}
