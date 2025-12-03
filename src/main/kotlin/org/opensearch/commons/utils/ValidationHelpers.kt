/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import java.net.URL
import java.util.regex.Pattern

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
val CLUSTER_PATTERN_REGEX = Regex("^(?=.{1,255}$)[a-z0-9*]([a-zA-Z0-9_*-]*:?[a-zA-Z0-9_*-]*)$")

// Valid ID characters = (All Base64 chars + "_-") to support UUID format and Base64 encoded IDs
private val VALID_ID_CHARS: Set<Char> = (('a'..'z') + ('A'..'Z') + ('0'..'9') + '+' + '/' + '_' + '-').toSet()

// Invalid characters in a new name field: [* ? < > | #]
private val INVALID_NAME_CHARS = "^\\*\\?<>|#"

fun validateUrl(urlString: String) {
    require(isValidUrl(urlString)) { "Invalid URL or unsupported" }
}

fun validateEmail(email: String) {
    require(isValidEmail(email)) { "Invalid email address" }
}

fun validateId(idString: String) {
    require(isValidId(idString)) { "Invalid characters in id : ${idString.filterNot { VALID_ID_CHARS.contains(it) }}" }
}

fun isValidUrl(urlString: String): Boolean {
    val url = URL(urlString) // throws MalformedURLException if URL is invalid
    return ("https" == url.protocol || "http" == url.protocol) // Support only http/https, other protocols not supported
}

/**
 * RFC 5322 compliant pattern matching: https://www.ietf.org/rfc/rfc5322.txt
 * Regex was based off of this post: https://stackoverflow.com/a/201378
 */
fun isValidEmail(email: String): Boolean {
    val validEmailPattern =
        Regex(
            "(?:[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+)*" +
                "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" +
                "|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}" +
                "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
            RegexOption.IGNORE_CASE,
        )
    return validEmailPattern.matches(email)
}

fun isValidId(idString: String): Boolean = idString.isNotBlank() && idString.all { VALID_ID_CHARS.contains(it) }

fun validateIamRoleArn(roleArn: String) {
    val roleArnRegex = Pattern.compile("^arn:aws(-[^:]+)?:iam::([0-9]{12}):([a-zA-Z_0-9+=,.@\\-_/]+)$")
    require(roleArnRegex.matcher(roleArn).find()) { "Invalid AWS role ARN: $roleArn " }
}

fun isValidName(name: String): Boolean {
    // Regex to restrict string so that it cannot start with [_, -, +],
    // contain two consecutive periods or contain invalid chars
    val regex = Regex("""^(?![_\-\+])(?!.*\.\.)[^$INVALID_NAME_CHARS]+$""")

    return name.matches(regex)
}

fun getInvalidNameChars(): String = INVALID_NAME_CHARS
