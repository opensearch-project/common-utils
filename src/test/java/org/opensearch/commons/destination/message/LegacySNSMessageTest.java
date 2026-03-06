/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LegacySNSMessageTest {

    @Test
    public void testCreateRoleArnMissingMessage() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LegacySNSMessage.Builder("sms").withMessage("dummyMessage").build()
        );
        assertEquals("Role arn is missing/invalid: null", ex.getMessage());
    }

    @Test
    public void testCreateTopicArnMissingMessage() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LegacySNSMessage.Builder("sms")
                .withMessage("dummyMessage")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .build()
        );
        assertEquals("Topic arn is missing/invalid: null", ex.getMessage());
    }

    @Test
    public void testCreateContentMissingMessage() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LegacySNSMessage.Builder("sms")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build()
        );
        assertEquals("Message content is missing", ex.getMessage());
    }

    @Test
    public void testInValidRoleMessage() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LegacySNSMessage.Builder("sms")
                .withMessage("dummyMessage")
                .withRole("dummyRole")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build()
        );
        assertEquals("Role arn is missing/invalid: dummyRole", ex.getMessage());
    }

    @Test
    public void testValidMessage() {
        LegacySNSMessage message = new LegacySNSMessage.Builder("sms")
            .withMessage("dummyMessage")
            .withRole("arn:aws:iam::853806060000:role/domain/abc")
            .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
            .build();

        assertEquals(LegacyDestinationType.LEGACY_SNS, message.getChannelType());
        assertEquals("sms", message.getChannelName());
        assertEquals("dummyMessage", message.getMessage());
        assertEquals("arn:aws:iam::853806060000:role/domain/abc", message.getRoleArn());
        assertEquals("arn:aws:sns:us-west-2:475313751589:test-notification", message.getTopicArn());
    }

    @Test
    public void testInValidChannelName() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LegacySNSMessage.Builder("")
                .withMessage("dummyMessage")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build()
        );
        assertEquals("Channel name must be defined", ex.getMessage());
    }
}
