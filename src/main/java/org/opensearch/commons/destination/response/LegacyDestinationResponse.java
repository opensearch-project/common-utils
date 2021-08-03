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

import java.io.IOException;

import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;

/**
 * This class is a place holder for destination response metadata
 */
public class LegacyDestinationResponse extends LegacyBaseResponse {

    private final String responseContent;

    private LegacyDestinationResponse(final String responseString, final Integer statusCode) {
        super(statusCode);
        if (responseString == null) {
            throw new IllegalArgumentException("Response is missing");
        }
        this.responseContent = responseString;
    }

    public LegacyDestinationResponse(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.responseContent = streamInput.readString();
    }

    public static class Builder {
        private String responseContent;
        private Integer statusCode;

        public LegacyDestinationResponse.Builder withResponseContent(String responseContent) {
            this.responseContent = responseContent;
            return this;
        }

        public LegacyDestinationResponse.Builder withStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public LegacyDestinationResponse build() {
            return new LegacyDestinationResponse(responseContent, statusCode);
        }
    }

    public String getResponseContent() {
        return this.responseContent;
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        super.writeTo(streamOutput);
        streamOutput.writeString(responseContent);
    }
}
