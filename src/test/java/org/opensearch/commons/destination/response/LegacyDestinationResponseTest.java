/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;

public class LegacyDestinationResponseTest {

    @Test
    public void testBuildingLegacyDestinationResponse() {
        LegacyDestinationResponse res = new LegacyDestinationResponse.Builder()
            .withStatusCode(200)
            .withResponseContent("Hello world")
            .build();

        assertEquals(200, res.statusCode);
        assertEquals("Hello world", res.getResponseContent());
    }

    @Test
    public void testRoundTrippingLegacyDestinationResponse() throws IOException {
        LegacyDestinationResponse res = new LegacyDestinationResponse.Builder()
            .withStatusCode(200)
            .withResponseContent("Hello world")
            .build();
        BytesStreamOutput out = new BytesStreamOutput();
        res.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacyDestinationResponse newRes = new LegacyDestinationResponse(in);

        assertEquals(res.statusCode, newRes.statusCode, "Round tripping doesn't work");
        assertEquals(res.getResponseContent(), newRes.getResponseContent(), "Round tripping doesn't work");
    }

    @Test
    public void testMissingLegacyDestinationResponse() {
        try {
            new LegacyDestinationResponse.Builder().withStatusCode(200).build();
            fail("Creating LegacyDestinationResponse without response content should fail");
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testMissingLegacyDestinationStatusCode() {
        try {
            new LegacyDestinationResponse.Builder().withResponseContent("Hello world").build();
            fail("Creating LegacyDestinationResponse without status code should fail");
        } catch (IllegalArgumentException ignored) {}
    }
}
