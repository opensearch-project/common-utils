package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.xcontent.ToXContent

class TargetTests {

    @Test
    fun `test default Target`() {
        val target = Target()
        assertEquals(Target.LOCAL, target.type)
        assertEquals("", target.endpoint)
    }

    @Test
    fun `test Target with custom type and endpoint`() {
        val target = Target("custom_type", "https://example.com")
        assertEquals("custom_type", target.type)
        assertEquals("https://example.com", target.endpoint)
    }

    @Test
    fun `test Target with empty type requires endpoint`() {
        assertThrows(IllegalArgumentException::class.java) {
            Target("", "")
        }
    }

    @Test
    fun `test Target requires endpoint for non-LOCAL type`() {
        assertThrows(IllegalArgumentException::class.java) {
            Target("custom_type", "")
        }
    }

    @Test
    fun `test Target stream serialization roundtrip`() {
        val target = Target("custom_type", "https://example.com")
        val out = BytesStreamOutput()
        target.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Target(sin)
        assertEquals(target, deserialized)
    }

    @Test
    fun `test Target XContent roundtrip`() {
        val target = Target("custom_type", "https://example.com")
        val builder = XContentFactory.jsonBuilder()
        target.toXContent(builder, ToXContent.EMPTY_PARAMS)
        val json = builder.toString()

        val parser = XContentType.JSON.xContent().createParser(null, null, json)
        parser.nextToken()
        val parsed = Target.parse(parser)
        assertEquals(target, parsed)
    }
}
