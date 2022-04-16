/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;

/**
 * This class holds the content of an CustomWebhook message
 */
public class LegacyCustomWebhookMessage extends LegacyBaseMessage {

    private final String message;
    private final String url;
    private final String scheme;
    private final String host;
    private final String method;
    private final int port;
    private String path;
    private final Map<String, String> queryParams;
    private Map<String, String> headerParams;

    private LegacyCustomWebhookMessage(
        final String destinationName,
        final String url,
        final String scheme,
        final String host,
        final Integer port,
        final String path,
        final String method,
        final Map<String, String> queryParams,
        final Map<String, String> headerParams,
        final String message
    ) {
        super(LegacyDestinationType.LEGACY_CUSTOM_WEBHOOK, destinationName, message);

        if (!Strings.isNullOrEmpty(url)) {
            setUrl(url.trim());
        }

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }

        this.scheme = Strings.isNullOrEmpty(scheme) ? "https" : scheme;
        this.port = port == null ? -1 : port;

        if (!Strings.isNullOrEmpty(path)) {
            if (!path.startsWith("/")) {
                this.path = "/" + path;
            }
        }

        if (Strings.isNullOrEmpty(url) && Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("Either fully qualified URL or host name should be provided");
        }

        if (Strings.isNullOrEmpty(method)) {
            // Default to POST for backwards compatibility
            this.method = "POST";
        } else if (!HttpPost.METHOD_NAME.equals(method) && !HttpPut.METHOD_NAME.equals(method) && !HttpPatch.METHOD_NAME.equals(method)) {
            throw new IllegalArgumentException("Invalid method supplied. Only POST, PUT and PATCH are allowed");
        } else {
            this.method = method;
        }

        this.message = message;
        this.url = url;
        this.host = host;
        this.queryParams = queryParams;
        this.headerParams = headerParams;
    }

    public LegacyCustomWebhookMessage(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.message = super.getMessageContent();
        this.url = streamInput.readOptionalString();
        this.scheme = null;
        this.host = null;
        this.method = streamInput.readOptionalString();
        this.port = -1;
        this.path = null;
        this.queryParams = null;
        if (streamInput.readBoolean()) {
            @SuppressWarnings("unchecked")
            Map<String, String> headerParams = (Map<String, String>) (Map) streamInput.readMap();
            this.headerParams = headerParams;
        }
    }

    @Override
    public String toString() {
        return "DestinationType: "
            + getChannelType()
            + ", DestinationName:"
            + destinationName
            + ", Url: "
            + url
            + ", scheme: "
            + scheme
            + ", Host: "
            + host
            + ", Port: "
            + port
            + ", Path: "
            + path
            + ", Method: "
            + method
            + ", Message: <...>";
    }

    public static class Builder {
        private String message;
        private final String destinationName;
        private String url;
        private String scheme;
        private String host;
        private Integer port;
        private String path;
        private String method;
        private Map<String, String> queryParams;
        private Map<String, String> headerParams;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
        }

        public LegacyCustomWebhookMessage.Builder withScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withHeaderParams(Map<String, String> headerParams) {
            this.headerParams = headerParams;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public LegacyCustomWebhookMessage build() {
            return new LegacyCustomWebhookMessage(
                this.destinationName,
                this.url,
                this.scheme,
                this.host,
                this.port,
                this.path,
                this.method,
                this.queryParams,
                this.headerParams,
                this.message
            );
        }
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaderParams() {
        return headerParams;
    }

    public URI getUri() {
        return buildUri(getUrl(), getScheme(), getHost(), getPort(), getPath(), getQueryParams());
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        super.writeTo(streamOutput);
        // Making LegacyCustomWebhookMessage streamable is purely to support the new pass through API from Alerting/ISM -> Notification
        // plugin
        // and it only supports LegacyCustomWebhookMessage when the url is already constructed by Alerting/ISM.
        if (Strings.isNullOrEmpty(getUrl())) {
            throw new IllegalStateException("Cannot use LegacyCustomWebhookMessage across transport wire without defining full url.");
        }
        streamOutput.writeOptionalString(url);
        streamOutput.writeOptionalString(method);
        streamOutput.writeBoolean(headerParams != null);
        if (headerParams != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headerParams = (Map<String, Object>) (Map) this.headerParams;
            streamOutput.writeMap(headerParams);
        }
    }
}
