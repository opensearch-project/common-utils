package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.randomUser
import org.opensearch.core.common.io.stream.StreamInput
import java.time.Instant

class CommentTests {
    @Test
    fun `test comment object`() {
        val user = randomUser()
        val comment = Comment(
            "123",
            "456",
            "alert",
            "content",
            Instant.now(),
            null,
            user
        )
        Assertions.assertNotNull(comment)
        val out = BytesStreamOutput()
        comment.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newComment = Comment(sin)
        assertEquals("123", newComment.id)
        assertEquals("456", newComment.entityId)
        assertEquals("alert", newComment.entityType)
        assertEquals("content", newComment.content)
        assertEquals(user, newComment.user)
    }
}
