/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications

import org.opensearch.commons.utils.validateUrl

private const val KEYSTORE_REFERENCE_PREFIX = "\${keystore:"
private val KEYSTORE_REFERENCE_PATTERN = Regex("\\$\\{keystore:([A-Za-z0-9_.-]+)}")

/**
 * Returns whether [value] is a complete reference to an OpenSearch keystore setting alias.
 */
fun isKeystoreReference(value: String): Boolean = KEYSTORE_REFERENCE_PATTERN.matches(value)

/**
 * Validates a notification URL or a complete OpenSearch keystore reference.
 */
fun validateUrlOrKeystoreReference(value: String) {
    if (value.contains(KEYSTORE_REFERENCE_PREFIX)) {
        require(isKeystoreReference(value)) { "Invalid OpenSearch keystore reference" }
    } else {
        validateUrl(value)
    }
}
