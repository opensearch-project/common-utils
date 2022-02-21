/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.utils

/**
 * Functional interface to create config data object using XContentParser
 */
fun interface EnumParser<E> {
    /**
     * Get Enum from tag or default value if not found
     * @param tag the tag
     * @return Enum corresponding to tag. default value if invalid tag.
     */
    fun fromTagOrDefault(tag: String): E
}
