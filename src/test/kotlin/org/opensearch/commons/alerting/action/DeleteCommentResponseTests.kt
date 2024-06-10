package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput

class DeleteCommentResponseTests {
    @Test
    fun `test delete comment response writing and parsing`() {
        val res = DeleteCommentResponse(id = "123")
        assertNotNull(res)
        assertEquals("123", res.commentId)

        val out = BytesStreamOutput()
        res.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRes = DeleteCommentResponse(sin)
        assertEquals("123", newRes.commentId)
    }
}
