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
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package org.opensearch.commons.destination.message;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;

import java.net.URI;
import java.util.Map;
import java.io.IOException;

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
    private Map<String, String> queryParams;
    private Map<String, String> headerParams;
    private final String userName;
    private final String password;

    private LegacyCustomWebhookMessage(final LegacyDestinationType destinationType,
                                 final String destinationName,
                                 final String url,
                                 final String scheme,
                                 final String host,
                                 final Integer port,
                                 final String path,
                                 final String method,
                                 final Map<String, String> queryParams,
                                 final Map<String, String> headerParams,
                                 final String userName,
                                 final String password,
                                 final String message) {

        super(destinationType, destinationName, message);

        if (LegacyDestinationType.CUSTOMWEBHOOK != destinationType) {
            throw new IllegalArgumentException("Channel Type does not match CustomWebhook");
        }

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

        if(Strings.isNullOrEmpty(url) && Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("Either fully qualified URL or host name should be provided");
        }

        if (Strings.isNullOrEmpty(method)){
            // Default to POST for backwards compatibility
            this.method = "POST";
        } else if (!HttpPost.METHOD_NAME.equals(method) && !HttpPut.METHOD_NAME.equals(method)
                && !HttpPatch.METHOD_NAME.equals(method)) {
            throw new IllegalArgumentException("Invalid method supplied. Only POST, PUT and PATCH are allowed");
        } else {
            this.method = method;
        }


        this.message = message;
        this.url = url;
        this.host = host;
        this.queryParams = queryParams;
        this.headerParams = headerParams;
        this.userName = userName;
        this.password = password;
    }

    public LegacyCustomWebhookMessage(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.message = super.getMessageContent();
        this.url = streamInput.readOptionalString();
        this.scheme = streamInput.readOptionalString();
        this.host = streamInput.readOptionalString();
        this.method = streamInput.readOptionalString();
        this.port = streamInput.readOptionalInt();
        this.path = streamInput.readOptionalString();
        if (streamInput.readBoolean()) {
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>)(Map)streamInput.readMap();
            this.queryParams = queryParams;
        }
        if (streamInput.readBoolean()) {
            @SuppressWarnings("unchecked")
            Map<String, String> headerParams = (Map<String, String>)(Map)streamInput.readMap();
            this.headerParams = headerParams;
        }
        this.userName = streamInput.readOptionalString();
        this.password = streamInput.readOptionalString();
    }

    @Override
    public String toString() {
        return "DestinationType: " + destinationType + ", DestinationName:" +  destinationName +
                ", Url: " + url + ", scheme: " + scheme + ", Host: " + host + ", Port: " +
                port + ", Path: " + path + ", Method: " + method + ", Message: " + message;
    }

    public static class Builder {
        private String message;
        private final LegacyDestinationType destinationType;
        private final String destinationName;
        private String url;
        private String scheme;
        private String host;
        private Integer port;
        private String path;
        private String method;
        private Map<String, String> queryParams;
        private Map<String, String> headerParams;
        private String userName;
        private String password;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
            this.destinationType = LegacyDestinationType.CUSTOMWEBHOOK;
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

        public LegacyCustomWebhookMessage.Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public LegacyCustomWebhookMessage.Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LegacyCustomWebhookMessage build() {
            return new LegacyCustomWebhookMessage(
                    this.destinationType, this.destinationName, this.url,
                    this.scheme, this.host, this.port, this.path, this.method, this.queryParams,
                    this.headerParams, this.userName, this.password, this.message);
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

    public String getMethod() { return method; }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaderParams() {
        return headerParams;
    }

    public URI getUri() {
        return buildUri(getUrl(), getScheme(), getHost(), getPort(), getPath(), getQueryParams());
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        super.writeTo(streamOutput);
        streamOutput.writeOptionalString(url);
        streamOutput.writeOptionalString(scheme);
        streamOutput.writeOptionalString(host);
        streamOutput.writeOptionalString(method);
        streamOutput.writeOptionalInt(port);
        streamOutput.writeOptionalString(path);
        streamOutput.writeBoolean(queryParams != null);
        if (queryParams != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> queryParams = (Map<String, Object>) (Map) this.queryParams;
            streamOutput.writeMap(queryParams);
        }
        streamOutput.writeBoolean(headerParams != null);
        if (headerParams != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headerParams = (Map<String, Object>) (Map) this.headerParams;
            streamOutput.writeMap(headerParams);
        }
        streamOutput.writeOptionalString(userName);
        streamOutput.writeOptionalString(password);
    }
}