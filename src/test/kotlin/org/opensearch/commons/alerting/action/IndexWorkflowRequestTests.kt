package org.opensearch.commons.alerting.action

import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.NamedWriteableAwareStreamInput
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.settings.Settings
import org.opensearch.commons.alerting.model.ChainedMonitorFindings
import org.opensearch.commons.alerting.model.CompositeInput
import org.opensearch.commons.alerting.model.Delegate
import org.opensearch.commons.alerting.model.Sequence
import org.opensearch.commons.alerting.randomWorkflow
import org.opensearch.commons.alerting.randomWorkflowWithDelegates
import org.opensearch.commons.utils.recreateObject
import org.opensearch.rest.RestRequest
import org.opensearch.search.SearchModule
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.UUID

class IndexWorkflowRequestTests {

    @Test
    fun `test index workflow post request`() {

        val req = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.POST,
            randomWorkflow()
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
            randomWorkflow()
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
            randomWorkflow()
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
            randomWorkflow()
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

    @Test
    fun `test validate`() {
        val req = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflow(monitorIds = emptyList())
        )
        Assertions.assertNotNull(req)
        // Empty input list
        var validate = req.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Delegates list can not be empty.;"))
        // Duplicate delegate
        val req1 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflow(monitorIds = listOf("1L", "1L", "2L"))
        )
        validate = req1.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Duplicate delegates not allowed"))
        // Sequence not correct
        var delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(1, "monitor-2"),
            Delegate(2, "monitor-3")
        )
        val req2 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflowWithDelegates(
                input = listOf(CompositeInput(Sequence(delegates = delegates)))
            )
        )
        validate = req2.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Sequence ordering of delegate monitor shouldn't contain duplicate order values"))
        // Chained finding sequence not correct
        delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(2, "monitor-2", ChainedMonitorFindings("monitor-1")),
            Delegate(3, "monitor-3", ChainedMonitorFindings("monitor-x"))
        )
        val req3 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflowWithDelegates(
                input = listOf(CompositeInput(Sequence(delegates = delegates)))
            )
        )
        validate = req3.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Chained Findings Monitor monitor-x doesn't exist in sequence"))
        // Order not correct
        delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(3, "monitor-2", ChainedMonitorFindings("monitor-1")),
            Delegate(2, "monitor-3", ChainedMonitorFindings("monitor-2"))
        )
        val req4 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflowWithDelegates(
                input = listOf(CompositeInput(Sequence(delegates = delegates)))
            )
        )
        validate = req4.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Chained Findings Monitor monitor-2 should be executed before monitor monitor-3"))
        // Max monitor size
        val monitorsIds = mutableListOf<String>()
        for (i in 0..25) {
            monitorsIds.add(UUID.randomUUID().toString())
        }
        val req5 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflow(
                monitorIds = monitorsIds
            )
        )
        validate = req5.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Delegates list can not be larger then 25."))
        // Input list empty
        val req6 = IndexWorkflowRequest(
            "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
            randomWorkflowWithDelegates(
                input = emptyList()
            )
        )
        validate = req6.validate()
        Assert.assertTrue(validate != null)
        Assert.assertTrue(validate!!.message!!.contains("Input list can not be empty."))
        // Input list multiple elements
        delegates = listOf(
            Delegate(1, "monitor-1")
        )
        try {
            IndexWorkflowRequest(
                "1234", 1L, 2L, WriteRequest.RefreshPolicy.IMMEDIATE, RestRequest.Method.PUT,
                randomWorkflowWithDelegates(
                    input = listOf(CompositeInput(Sequence(delegates = delegates)), CompositeInput(Sequence(delegates = delegates)))
                )
            )
        } catch (ex: Exception) {
            Assert.assertTrue(ex is IllegalArgumentException)
            Assert.assertTrue(ex.message!!.contains("Workflows can only have 1 search input."))
        }
    }
}
