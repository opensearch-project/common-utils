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
        assertEquals("", target.arn)
    }

    @Test
    fun `test Target with custom type endpoint and arn`() {
        val target = Target("AOS_DOMAIN", "https://my-domain.us-west-2.es.amazonaws.com", "arn:aws:es:us-west-2:123456789012:domain/my-domain")
        assertEquals("AOS_DOMAIN", target.type)
        assertEquals("https://my-domain.us-west-2.es.amazonaws.com", target.endpoint)
        assertEquals("arn:aws:es:us-west-2:123456789012:domain/my-domain", target.arn)
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
            Target("AOS_DOMAIN", "", "arn:aws:es:us-west-2:123456789012:domain/my-domain")
        }
    }

    @Test
    fun `test Target requires arn for non-LOCAL type`() {
        assertThrows(IllegalArgumentException::class.java) {
            Target("AOS_DOMAIN", "https://my-domain.us-west-2.es.amazonaws.com", "")
        }
    }

    @Test
    fun `test Target LOCAL type does not require arn`() {
        val target = Target(Target.LOCAL, "", "")
        assertEquals(Target.LOCAL, target.type)
        assertEquals("", target.endpoint)
        assertEquals("", target.arn)
    }

    @Test
    fun `test Target stream serialization roundtrip`() {
        val target = Target("AOS_DOMAIN", "https://my-domain.us-west-2.es.amazonaws.com", "arn:aws:es:us-west-2:123456789012:domain/my-domain")
        val out = BytesStreamOutput()
        target.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Target(sin)
        assertEquals(target, deserialized)
    }

    @Test
    fun `test Target stream serialization roundtrip LOCAL`() {
        val target = Target()
        val out = BytesStreamOutput()
        target.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val deserialized = Target(sin)
        assertEquals(target, deserialized)
    }

    @Test
    fun `test Target XContent roundtrip`() {
        val target = Target("AOS_DOMAIN", "https://my-domain.us-west-2.es.amazonaws.com", "arn:aws:es:us-west-2:123456789012:domain/my-domain")
        val builder = XContentFactory.jsonBuilder()
        target.toXContent(builder, ToXContent.EMPTY_PARAMS)
        val json = builder.toString()

        val parser = XContentType.JSON.xContent().createParser(null, null, json)
        parser.nextToken()
        val parsed = Target.parse(parser)
        assertEquals(target, parsed)
    }

    @Test
    fun `test Target XContent parse without arn field defaults to empty`() {
        val json = """{"type":"local","endpoint":""}"""
        val parser = XContentType.JSON.xContent().createParser(null, null, json)
        parser.nextToken()
        val parsed = Target.parse(parser)
        assertEquals("local", parsed.type)
        assertEquals("", parsed.endpoint)
        assertEquals("", parsed.arn)
    }

    @Test
    fun `test Target XContent includes arn field`() {
        val target = Target("AOS_DOMAIN", "https://my-domain.us-east-1.es.amazonaws.com", "arn:aws:es:us-east-1:123456789012:domain/my-domain")
        val builder = XContentFactory.jsonBuilder()
        target.toXContent(builder, ToXContent.EMPTY_PARAMS)
        val json = builder.toString()
        assert(json.contains("\"arn\":\"arn:aws:es:us-east-1:123456789012:domain/my-domain\""))
    }
}
