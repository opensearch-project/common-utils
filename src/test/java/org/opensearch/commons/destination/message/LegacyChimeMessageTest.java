/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.opensearch.commons.destination.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;

public class LegacyChimeMessageTest {

    @Test
    public void testBuildingLegacyChimeMessage() {
        LegacyChimeMessage message = new LegacyChimeMessage.Builder("custom_webhook")
            .withMessage("Hello world")
            .withUrl("https://amazon.com")
            .build();

        assertEquals("custom_webhook", message.destinationName);
        assertEquals(LegacyDestinationType.LEGACY_CHIME, message.getChannelType());
        assertEquals("Hello world", message.getMessageContent());
        assertEquals("https://amazon.com", message.url);
    }

    @Test
    public void testRoundTrippingLegacyChimeMessage() throws IOException {
        LegacyChimeMessage message = new LegacyChimeMessage.Builder("custom_webhook")
            .withMessage("Hello world")
            .withUrl("https://amazon.com")
            .build();
        BytesStreamOutput out = new BytesStreamOutput();
        message.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacyChimeMessage newMessage = new LegacyChimeMessage(in);

        assertEquals(newMessage.destinationName, message.destinationName);
        assertEquals(newMessage.getChannelType(), message.getChannelType());
        assertEquals(newMessage.getMessageContent(), message.getMessageContent());
        assertEquals(newMessage.url, message.url);
    }

    @Test
    public void testContentMissingMessage() {
        try {
            new LegacyChimeMessage.Builder("custom_webhook").withUrl("https://amazon.com").build();
            fail("Building legacy chime message without message should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Message content is missing", e.getMessage());
        }
    }

    @Test
    public void testUrlMissingMessage() {
        try {
            new LegacyChimeMessage.Builder("custom_webhook").withMessage("Hello world").build();
            fail("Building legacy chime message without url should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("url is invalid or empty", e.getMessage());
        }
    }

    @Test
    public void testMissingDestinationName() {
        try {
            new LegacyChimeMessage.Builder(null).withMessage("Hello world").withUrl("https://amazon.com").build();
            fail("Building legacy chime message with null destination name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Channel name must be defined", e.getMessage());
        }
    }
}
