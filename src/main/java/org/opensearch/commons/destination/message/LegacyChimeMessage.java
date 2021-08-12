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
 * This class holds the contents of an Chime message
 */
public class LegacyChimeMessage extends LegacyBaseMessage {
    private final String message;

    private LegacyChimeMessage(final String destinationName, final String url, final String message) {
        super(LegacyDestinationType.LEGACY_CHIME, destinationName, message, url);

        if (Strings.isNullOrEmpty(message)) {
            throw new IllegalArgumentException("Message content is missing");
        }

        this.message = message;
    }

    public LegacyChimeMessage(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.message = super.getMessageContent();
    }

    @Override
    public String toString() {
        return "DestinationType: " + getChannelType() + ", DestinationName:" + destinationName + ", Url: " + url + ", Message: <...>";
    }

    public static class Builder {
        private String message;
        private final String destinationName;
        private String url;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
        }

        public LegacyChimeMessage.Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public LegacyChimeMessage.Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public LegacyChimeMessage build() {
            return new LegacyChimeMessage(this.destinationName, this.url, this.message);
        }
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }
}
