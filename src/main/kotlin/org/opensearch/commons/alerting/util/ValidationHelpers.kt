/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

class ValidationHelpers {
    companion object {
        /**
         * This regex asserts that the string:
         *  Starts with a lowercase letter, or digit
         *  Contains a sequence of characters followed by an optional colon and another sequence of characters
         *  The sequences of characters can include lowercase letters, uppercase letters, digits, underscores, or hyphens
         *  The total length of the string can range from 1 to 255 characters
         */
        val CLUSTER_NAME_REGEX = Regex("^(?=.{1,255}$)[a-z0-9]([a-zA-Z0-9_-]*:?[a-zA-Z0-9_-]*)$")

        /**
         * This regex asserts that the string:
         *  Starts with a lowercase letter, digit, or asterisk
         *  Contains a sequence of characters followed by an optional colon and another sequence of characters
         *  The sequences of characters can include lowercase letters, uppercase letters, digits, underscores, asterisks, or hyphens
         *  The total length of the string can range from 1 to 255 characters
         */
        val CLUSTER_PATTERN_REGEX = Regex("^(?=.{1,255}$)[a-z0-9]([a-zA-Z0-9_-]*:?[a-zA-Z0-9_-]*)$")
    }
}