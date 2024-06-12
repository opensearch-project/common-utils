package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.Comment
import org.opensearch.commons.alerting.randomUser
import org.opensearch.core.common.io.stream.StreamInput
import java.time.Instant

class IndexCommentResponseTests {
    @Test
    fun `test index comment response with comment`() {
        val comment = Comment(
            "123",
            "456",
            "comment",
            Instant.now(),
            Instant.now(),
            randomUser()
        )
        val req = IndexCommentResponse("1234", 1L, 2L, comment)
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = IndexCommentResponse(sin)
        Assertions.assertEquals("1234", newReq.id)
        Assertions.assertEquals(1L, newReq.seqNo)
        Assertions.assertEquals(2L, newReq.primaryTerm)
        Assertions.assertNotNull(newReq.comment)
    }
}
