/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.response;

import java.io.IOException;

import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;

/**
 * This class holds the generic response attributes
 */
public abstract class LegacyBaseResponse implements Writeable {
    protected Integer statusCode;

    LegacyBaseResponse(final Integer statusCode) {
        if (statusCode == null) {
            throw new IllegalArgumentException("status code is invalid");
        }
        this.statusCode = statusCode;
    }

    public LegacyBaseResponse(StreamInput streamInput) throws IOException {
        this.statusCode = streamInput.readInt();
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        streamOutput.writeInt(statusCode);
    }
}
