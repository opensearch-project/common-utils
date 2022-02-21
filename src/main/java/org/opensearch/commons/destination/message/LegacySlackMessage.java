/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.message;

import java.io.IOException;

import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;

/**
 * This class holds the content of an Slack message
 */
public class LegacySlackMessage extends LegacyBaseMessage {
    private final String message;

    private LegacySlackMessage(final String destinationName, final String url, final String message) {
        super(LegacyDestinationType.LEGACY_SLACK, destinationName, message, url);

        if (Strings.isNullOrEmpty(url)) { // add URL validation
            throw new IllegalArgumentException("Fully qualified URL is missing/invalid: " + url);
        }

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }

        this.message = message;
    }

    public LegacySlackMessage(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.message = super.getMessageContent();
    }

    @Override
    public String toString() {
        return "DestinationType: " + getChannelType() + ", DestinationName:" + destinationName + ", Url: " + url + ", Message: <...>";
    }

    public static class Builder {
        private String message;
        private String destinationName;
        private String url;

        public Builder(String channelName) {
            this.destinationName = channelName;
        }

        public LegacySlackMessage.Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public LegacySlackMessage.Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public LegacySlackMessage build() {
            return new LegacySlackMessage(this.destinationName, this.url, this.message);
        }
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }
}
