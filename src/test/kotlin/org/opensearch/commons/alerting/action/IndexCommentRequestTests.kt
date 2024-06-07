package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.rest.RestRequest

class IndexCommentRequestTests {
    @Test
    fun `test index comment post request`() {
        val req = IndexCommentRequest("123", "456", 1L, 2L, RestRequest.Method.POST, "comment")
        assertNotNull(req)
        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexCommentRequest(sin)
        assertEquals("123", newReq.entityId)
        assertEquals("456", newReq.commentId)
        assertEquals(1L, newReq.seqNo)
        assertEquals(2L, newReq.primaryTerm)
        assertEquals(RestRequest.Method.POST, newReq.method)
        assertEquals("comment", newReq.content)
    }

    @Test
    fun `test index comment put request`() {
        val req = IndexCommentRequest("123", "456", 1L, 2L, RestRequest.Method.PUT, "comment")
        assertNotNull(req)
        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexCommentRequest(sin)
        assertEquals("123", newReq.entityId)
        assertEquals("456", newReq.commentId)
        assertEquals(1L, newReq.seqNo)
        assertEquals(2L, newReq.primaryTerm)
        assertEquals(RestRequest.Method.PUT, newReq.method)
        assertEquals("comment", newReq.content)
    }
}
