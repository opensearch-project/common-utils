/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;

public class LegacySlackMessageTest {

    @Test
    public void testBuildingLegacySlackMessage() {
        LegacySlackMessage message = new LegacySlackMessage.Builder("custom_webhook")
            .withMessage("Hello world")
            .withUrl("https://amazon.com")
            .build();

        assertEquals("custom_webhook", message.destinationName);
        assertEquals(LegacyDestinationType.LEGACY_SLACK, message.getChannelType());
        assertEquals("Hello world", message.getMessageContent());
        assertEquals("https://amazon.com", message.url);
    }

    @Test
    public void testRoundTrippingLegacySlackMessage() throws IOException {
        LegacySlackMessage message = new LegacySlackMessage.Builder("custom_webhook")
            .withMessage("Hello world")
            .withUrl("https://amazon.com")
            .build();
        BytesStreamOutput out = new BytesStreamOutput();
        message.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacySlackMessage newMessage = new LegacySlackMessage(in);

        assertEquals(newMessage.destinationName, message.destinationName);
        assertEquals(newMessage.getChannelType(), message.getChannelType());
        assertEquals(newMessage.getMessageContent(), message.getMessageContent());
        assertEquals(newMessage.url, message.url);
    }

    @Test
    public void testContentMissingMessage() {
        try {
            new LegacySlackMessage.Builder("custom_webhook").withUrl("https://amazon.com").build();
            fail("Building legacy slack message without message should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Message content is missing", e.getMessage());
        }
    }

    @Test
    public void testUrlMissingMessage() {
        try {
            new LegacySlackMessage.Builder("custom_webhook").withMessage("Hello world").build();
            fail("Building legacy slack message without url should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("url is invalid or empty", e.getMessage());
        }
    }

    @Test
    public void testMissingDestinationName() {
        try {
            new LegacySlackMessage.Builder(null).withMessage("Hello world").withUrl("https://amazon.com").build();
            fail("Building legacy slack message with null destination name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Channel name must be defined", e.getMessage());
        }
    }

    @Test
    public void testUrlEmptyMessage() {
        try {
            new LegacySlackMessage.Builder("custom_webhook").withMessage("Hello world").withUrl("").build();
            fail("Building legacy slack message with empty url should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Fully qualified URL is missing/invalid: ", e.getMessage());
        }
    }
}
