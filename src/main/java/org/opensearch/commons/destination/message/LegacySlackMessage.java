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
