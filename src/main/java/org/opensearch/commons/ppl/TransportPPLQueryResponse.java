/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.ppl;

import java.io.IOException;

import org.opensearch.core.action.ActionResponse;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;

/**
 * This is a copied, reduced version of SQL Plugin's TransportPPLQueryResponse
 */
public class TransportPPLQueryResponse extends ActionResponse {

    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    private final String result;
    private final String contentType;

    public TransportPPLQueryResponse(String result) {
        this.result = result;
        this.contentType = CONTENT_TYPE;
    }

    public TransportPPLQueryResponse(StreamInput in) throws IOException {
        super(in);
        result = in.readString();
        contentType = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        // super.writeTo(out) is a no-op, matching the SQL plugin's implementation
        out.writeString(result);
        out.writeString(CONTENT_TYPE);
    }

    public String getResult() {
        return result;
    }

    public String getContentType() {
        return contentType;
    }
}
