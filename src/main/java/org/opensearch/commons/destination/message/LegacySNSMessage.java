/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import java.io.IOException;

import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.commons.destination.util.Util;

/**
 * This class holds the content of an SNS message
 */
public class LegacySNSMessage extends LegacyBaseMessage {

    private final String subject;
    private final String message;
    private final String roleArn;
    private final String topicArn;
    private final String clusterName;

    private LegacySNSMessage(
        final String destinationName,
        final String roleArn,
        final String topicArn,
        final String clusterName,
        final String subject,
        final String message
    ) {
        super(LegacyDestinationType.LEGACY_SNS, destinationName, message);

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }
        if (Strings.isNullOrEmpty(roleArn) || !Util.isValidIAMArn(roleArn)) {
            throw new IllegalArgumentException("Role arn is missing/invalid: " + roleArn);
        }

        if (Strings.isNullOrEmpty(topicArn) || !Util.isValidSNSArn(topicArn)) {
            throw new IllegalArgumentException("Topic arn is missing/invalid: " + topicArn);
        }

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }

        this.subject = subject;
        this.message = message;
        this.roleArn = roleArn;
        this.topicArn = topicArn;
        this.clusterName = clusterName;
    }

    public LegacySNSMessage(StreamInput streamInput) throws java.io.IOException {
        super(streamInput);
        this.subject = streamInput.readString();
        this.message = super.getMessageContent();
        this.roleArn = streamInput.readString();
        this.topicArn = streamInput.readString();
        this.clusterName = streamInput.readString();
    }

    @Override
    public String toString() {
        return "DestinationType: "
            + getChannelType()
            + ", DestinationName: "
            + destinationName
            + ", RoleARn: "
            + roleArn
            + ", TopicArn: "
            + topicArn
            + ", ClusterName: "
            + clusterName
            + ", Subject: "
            + subject
            + ", Message: "
            + message;
    }

    public static class Builder {
        private final String destinationName;
        private String subject;
        private String message;
        private String roleArn;
        private String topicArn;
        private String clusterName;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withRole(String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        public Builder withTopicArn(String topicArn) {
            this.topicArn = topicArn;
            return this;
        }

        public Builder withClusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public LegacySNSMessage build() {
            return new LegacySNSMessage(this.destinationName, this.roleArn, this.topicArn, this.clusterName, this.subject, this.message);
        }
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        super.writeTo(streamOutput);
        streamOutput.writeString(subject);
        streamOutput.writeString(message);
        streamOutput.writeString(roleArn);
        streamOutput.writeString(topicArn);
        streamOutput.writeString(clusterName);
    }
}
