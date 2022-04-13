/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;

public class LegacyEmailMessageTest {

    @Test
    public void testBuildingLegacyEmailMessage() {
        LegacyEmailMessage message = new LegacyEmailMessage.Builder("email")
            .withAccountName("test_email")
            .withHost("smtp.test.com")
            .withPort(123)
            .withMethod("none")
            .withFrom("test@email.com")
            .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
            .withSubject("Test Subject")
            .withMessage("Hello world")
            .build();

        assertEquals("email", message.destinationName);
        assertEquals(LegacyDestinationType.LEGACY_EMAIL, message.getChannelType());
        assertEquals("test_email", message.getAccountName());
        assertEquals("smtp.test.com", message.getHost());
        assertEquals(123, message.getPort());
        assertEquals("none", message.getMethod());
        assertEquals("test@email.com", message.getFrom());
        assertEquals(Arrays.asList("test2@email.com", "test3@email.com"), message.getRecipients());
        assertEquals("Test Subject", message.getSubject());
        assertEquals("Hello world", message.getMessage());
    }

    @Test
    public void testRoundTrippingLegacyEmailMessage() throws IOException {
        LegacyEmailMessage message = new LegacyEmailMessage.Builder("email")
            .withAccountName("test_email")
            .withHost("smtp.test.com")
            .withPort(123)
            .withMethod("none")
            .withFrom("test@email.com")
            .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
            .withSubject("Test Subject")
            .withMessage("Hello world")
            .build();
        BytesStreamOutput out = new BytesStreamOutput();
        message.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacyEmailMessage newMessage = new LegacyEmailMessage(in);

        assertEquals(newMessage.destinationName, message.destinationName);
        assertEquals(newMessage.getChannelType(), message.getChannelType());
        assertEquals(newMessage.getAccountName(), message.getAccountName());
        assertEquals(newMessage.getHost(), message.getHost());
        assertEquals(newMessage.getPort(), message.getPort());
        assertEquals(newMessage.getMethod(), message.getMethod());
        assertEquals(newMessage.getFrom(), message.getFrom());
        assertEquals(newMessage.getRecipients(), message.getRecipients());
        assertEquals(newMessage.getSubject(), message.getSubject());
        assertEquals(newMessage.getMessage(), message.getMessage());
    }

    @Test
    public void testContentMissingMessage() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .build();
            fail("Building legacy email message without message should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Message content is missing", e.getMessage());
        }
    }

    @Test
    public void testMissingDestinationName() {
        try {
            new LegacyEmailMessage.Builder(null)
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with null destination name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Channel name must be defined", e.getMessage());
        }
    }

    @Test
    public void testUnsupportedMethods() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("unsupported")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with unsupported method should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid method supplied. Only none, ssl and start_tls are allowed", e.getMessage());
        }
    }

    @Test
    public void testAccountNameMissingOrEmpty() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with missing account name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Account name should be provided", e.getMessage());
        }

        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with empty account name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Account name should be provided", e.getMessage());
        }
    }

    @Test
    public void testHostMissingOrEmpty() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with missing host should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Host name should be provided", e.getMessage());
        }

        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with empty host should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Host name should be provided", e.getMessage());
        }
    }

    @Test
    public void testFromMissingOrEmpty() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with missing from should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("From address should be provided", e.getMessage());
        }

        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("")
                .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with empty from should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("From address should be provided", e.getMessage());
        }
    }

    @Test
    public void testRecipientsMissingOrEmpty() {
        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with missing recipients should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("List of recipients should be provided", e.getMessage());
        }

        try {
            new LegacyEmailMessage.Builder("email")
                .withAccountName("test_email")
                .withHost("smtp.test.com")
                .withPort(123)
                .withMethod("none")
                .withFrom("test@email.com")
                .withRecipients(List.of())
                .withSubject("Test Subject")
                .withMessage("Hello world")
                .build();
            fail("Building legacy email message with empty recipients should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("List of recipients should be provided", e.getMessage());
        }
    }

    @Test
    public void testSubjectDefaultsToDestinationNameWhenMissingOrEmpty() {
        LegacyEmailMessage message = new LegacyEmailMessage.Builder("email")
            .withAccountName("test_email")
            .withHost("smtp.test.com")
            .withPort(123)
            .withMethod("none")
            .withFrom("test@email.com")
            .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
            .withMessage("Hello world")
            .build();

        assertEquals("email", message.getSubject());

        message = new LegacyEmailMessage.Builder("email")
            .withAccountName("test_email")
            .withHost("smtp.test.com")
            .withPort(123)
            .withMethod("none")
            .withFrom("test@email.com")
            .withRecipients(Arrays.asList("test2@email.com", "test3@email.com"))
            .withSubject("")
            .withMessage("Hello world")
            .build();

        assertEquals("email", message.getSubject());
    }
}
