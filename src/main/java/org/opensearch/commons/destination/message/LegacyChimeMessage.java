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

import java.io.IOException;

import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;

/**
 * This class holds the contents of an Chime message
 */
public class LegacyChimeMessage extends LegacyBaseMessage {
    private final String message;
    private LegacyChimeMessage(final LegacyDestinationType destinationType,
                         final String destinationName,
                         final String url,
                         final String message) {

        super(destinationType, destinationName, message, url);

        if (LegacyDestinationType.CHIME != destinationType) {
            throw new IllegalArgumentException("Channel Type does not match CHIME");
        }

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
        return "DestinationType: " + destinationType + ", DestinationName:" +  destinationName +
                ", Url: " + url + ", Message: " + message;
    }

    public static class Builder {
        private String message;
        private final LegacyDestinationType destinationType;
        private final String destinationName;
        private String url;

        public Builder(String destinationName) {
            this.destinationName = destinationName;
            this.destinationType = LegacyDestinationType.CHIME;
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
            return new LegacyChimeMessage(this.destinationType, this.destinationName, this.url,
                    this.message);
        }
    }

    public String getUrl() {
        return url;
    }
}