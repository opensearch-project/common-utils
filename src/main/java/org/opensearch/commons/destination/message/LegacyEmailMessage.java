/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.commons.notifications.model.MethodType;

/**
 * This class holds the content of an CustomWebhook message
 */
public class LegacyEmailMessage extends LegacyBaseMessage {

    private final String accountName;
    private final String host;
    private final int port;
    private final String method;
    private final String from;
    private final List<String> recipients;
    private final String subject;
    private final String message;

    private LegacyEmailMessage(
        final String destinationName,
        final String accountName,
        final String host,
        final Integer port,
        final String method,
        final String from,
        final List<String> recipients,
        final String subject,
        final String message
    ) {
        super(LegacyDestinationType.LEGACY_EMAIL, destinationName, message);

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }

        if (Strings.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException("Account name should be provided");
        }

        if (Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("Host name should be provided");
        }

        if (Strings.isNullOrEmpty(from)) {
            throw new IllegalArgumentException("From address should be provided");
        }

        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("List of recipients should be provided");
        }

        this.message = message;
        this.accountName = accountName;
        this.host = host;
        this.port = port == null ? 25 : port;

        if (Strings.isNullOrEmpty(method)) {
            // Default to "none"
            this.method = "none";
        } else if (!MethodType.NONE.toString().equals(method)
            && !MethodType.SSL.toString().equals(method)
            && !MethodType.START_TLS.toString().equals(method)) {
            throw new IllegalArgumentException("Invalid method supplied. Only none, ssl and start_tls are allowed");
        } else {
            this.method = method;
        }

        this.from = from;
        this.recipients = recipients;
        this.subject = Strings.isNullOrEmpty(subject) ? destinationName : subject;
    }

    public LegacyEmailMessage(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.message = super.getMessageContent();
        this.accountName = streamInput.readString();
        this.host = streamInput.readString();
        this.port = streamInput.readInt();
        this.method = streamInput.readString();
        this.from = streamInput.readString();
        this.recipients = streamInput.readStringList();
        this.subject = streamInput.readString();
    }

    @Override
    public String toString() {
        return "DestinationType: "
            + getChannelType()
            + ", DestinationName:"
            + destinationName
            + ", AccountName:"
            + accountName
            + ", From: "
            + from
            + ", Host: "
            + host
            + ", Port: "
            + port
            + ", Method: "
            + method
            + ", Subject: <...>"
            + ", Message: <...>";
    }

    public static class Builder {
        private final String destinationName;
        private String accountName;
        private String host;
        private Integer port;
        private String method;
        private String from;
        private List<String> recipients;
        private String subject;
        private String message;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
        }

        public LegacyEmailMessage.Builder withAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public LegacyEmailMessage.Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public LegacyEmailMessage.Builder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public LegacyEmailMessage.Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public LegacyEmailMessage.Builder withFrom(String from) {
            this.from = from;
            return this;
        }

        public LegacyEmailMessage.Builder withRecipients(List<String> recipients) {
            this.recipients = recipients;
            return this;
        }

        public LegacyEmailMessage.Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public LegacyEmailMessage.Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public LegacyEmailMessage build() {
            return new LegacyEmailMessage(
                this.destinationName,
                this.accountName,
                this.host,
                this.port,
                this.method,
                this.from,
                this.recipients,
                this.subject,
                this.message
            );
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMethod() {
        return method;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public URI getUri() {
        return buildUri(null, null, host, port, null, null);
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        super.writeTo(streamOutput);
        streamOutput.writeString(accountName);
        streamOutput.writeString(host);
        streamOutput.writeInt(port);
        streamOutput.writeString(method);
        streamOutput.writeString(from);
        streamOutput.writeStringCollection(recipients);
        streamOutput.writeString(subject);
    }
}
