/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LegacySNSMessageTest {

    @Test
    public void testCreateRoleArnMissingMessage() {
        try {
            LegacySNSMessage message = new LegacySNSMessage.Builder("sms").withMessage("dummyMessage").build();
        } catch (Exception ex) {
            assertEquals("Role arn is missing/invalid: null", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testCreateTopicArnMissingMessage() {
        try {
            LegacySNSMessage message = new LegacySNSMessage.Builder("sms")
                .withMessage("dummyMessage")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .build();
        } catch (Exception ex) {
            assertEquals("Topic arn is missing/invalid: null", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testCreateContentMissingMessage() {
        try {
            LegacySNSMessage message = new LegacySNSMessage.Builder("sms")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build();
        } catch (Exception ex) {
            assertEquals("Message content is missing", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testInValidRoleMessage() {
        try {
            LegacySNSMessage message = new LegacySNSMessage.Builder("sms")
                .withMessage("dummyMessage")
                .withRole("dummyRole")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build();
        } catch (Exception ex) {
            assertEquals("Role arn is missing/invalid: dummyRole", ex.getMessage());
            throw ex;
        }
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
        try {
            LegacySNSMessage message = new LegacySNSMessage.Builder("")
                .withMessage("dummyMessage")
                .withRole("arn:aws:iam::853806060000:role/domain/abc")
                .withTopicArn("arn:aws:sns:us-west-2:475313751589:test-notification")
                .build();
        } catch (Exception ex) {
            assertEquals("Channel name must be defined", ex.getMessage());
            throw ex;
        }
    }
}
