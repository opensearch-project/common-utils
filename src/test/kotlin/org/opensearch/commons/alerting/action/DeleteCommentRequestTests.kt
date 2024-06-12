package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput

class DeleteCommentRequestTests {
    @Test
    fun `test delete comment request writing and parsing`() {
        val req = DeleteCommentRequest("1234")
        assertNotNull(req)
        assertEquals("1234", req.commentId)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = DeleteCommentRequest(sin)
        assertEquals("1234", newReq.commentId)
    }
}
