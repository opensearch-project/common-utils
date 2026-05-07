/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.ppl;

import java.io.IOException;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;

/**
 * This is a copied, reduced version of SQL Plugin's TransportPPLQueryRequest
 */
public class TransportPPLQueryRequest extends ActionRequest {

    // Ordinals must match org.opensearch.sql.protocol.response.format.JsonResponseFormatter.Style
    private enum Style {
        COMPACT,
        PRETTY
    }

    // actually used fields
    private final String pplQuery;
    private final String jsonContentString;
    private final String path;

    // fields put here to match the original TransportPPLQueryRequest
    private final String format;
    private final String explainMode;
    private final boolean sanitize;
    private final Style style;
    private final boolean profile;
    private final String queryId;

    public TransportPPLQueryRequest(String pplQuery, String jsonContentString, String path) {
        this.pplQuery = pplQuery;
        this.jsonContentString = jsonContentString;
        this.path = path;
        this.format = "";
        this.explainMode = null;
        this.sanitize = true;
        this.style = Style.COMPACT;
        this.profile = false;
        this.queryId = null;
    }

    public TransportPPLQueryRequest(StreamInput in) throws IOException {
        super(in);
        this.pplQuery = in.readOptionalString();
        this.format = in.readOptionalString();
        this.explainMode = in.readOptionalString();
        this.jsonContentString = in.readOptionalString();
        this.path = in.readOptionalString();
        this.sanitize = in.readBoolean();
        this.style = in.readEnum(Style.class);
        this.profile = in.readBoolean();
        this.queryId = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(pplQuery);
        out.writeOptionalString(format);
        out.writeOptionalString(explainMode);
        out.writeOptionalString(jsonContentString);
        out.writeOptionalString(path);
        out.writeBoolean(sanitize);
        out.writeEnum(style);
        out.writeBoolean(profile);
        out.writeOptionalString(queryId);
    }

    public String getPplQuery() {
        return pplQuery;
    }

    public String getJsonContentString() {
        return jsonContentString;
    }

    public String getPath() {
        return path;
    }

    public String getFormat() {
        return format;
    }

    public String getExplainMode() {
        return explainMode;
    }

    public boolean getSanitize() {
        return sanitize;
    }

    public Style getStyle() {
        return style;
    }

    public boolean getProfile() {
        return profile;
    }

    public String getQueryId() {
        return queryId;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
