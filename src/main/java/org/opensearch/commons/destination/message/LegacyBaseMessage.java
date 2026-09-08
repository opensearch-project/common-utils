/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.opensearch.core.common.Strings;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.common.io.stream.Writeable;

/**
 * This class holds the generic parameters required for a
 * message.
 */
public abstract class LegacyBaseMessage implements Writeable {

    private final LegacyDestinationType destinationType;
    protected String destinationName;
    protected String url;
    private final String content;

    LegacyBaseMessage(final LegacyDestinationType destinationType, final String destinationName, final String content) {
        if (destinationType == null) {
            throw new IllegalArgumentException("Channel type must be defined");
        }
        if (!Strings.hasLength(destinationName)) {
            throw new IllegalArgumentException("Channel name must be defined");
        }
        this.destinationType = destinationType;
        this.destinationName = destinationName;
        this.content = content;
    }

    LegacyBaseMessage(final LegacyDestinationType destinationType, final String destinationName, final String content, final String url) {
        this(destinationType, destinationName, content);
        if (url == null) {
            throw new IllegalArgumentException("url is invalid or empty");
        }
        this.url = url;
    }

    LegacyBaseMessage(StreamInput streamInput) throws IOException {
        this.destinationType = streamInput.readEnum(LegacyDestinationType.class);
        this.destinationName = streamInput.readString();
        this.url = streamInput.readOptionalString();
        this.content = streamInput.readString();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LegacyDestinationType getChannelType() {
        return destinationType;
    }

    public String getChannelName() {
        return destinationName;
    }

    public String getMessageContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public URI getUri() {
        return buildUri(getUrl().trim(), null, null, -1, null, null);
    }

    protected URI buildUri(String endpoint, String scheme, String host, int port, String path, Map<String, String> queryParams) {
        try {
            if (Strings.isNullOrEmpty(endpoint)) {
                if (Strings.isNullOrEmpty(scheme)) {
                    scheme = "https";
                }
                return new URI(scheme, null, host, port, path, buildQueryString(queryParams), null);
            }
            return new URI(endpoint);
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Error creating URI");
        }
    }

    private static String buildQueryString(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        for (Entry<String, String> param : queryParams.entrySet()) {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(param.getKey()).append('=').append(param.getValue());
        }
        return query.toString();
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        streamOutput.writeEnum(destinationType);
        streamOutput.writeString(destinationName);
        streamOutput.writeOptionalString(url);
        streamOutput.writeString(content);
    }
}
