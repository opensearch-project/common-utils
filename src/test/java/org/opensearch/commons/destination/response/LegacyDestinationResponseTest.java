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
