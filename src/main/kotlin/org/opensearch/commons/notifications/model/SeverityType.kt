/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.opensearch.commons.utils.EnumParser

/**
 * Notification severity type
 */
enum class SeverityType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    HIGH("high") {
        override fun toString(): String {
            return tag
        }
    },
    INFO("info") {
        override fun toString(): String {
            return tag
        }
    },
    CRITICAL("critical") {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get SeverityType from tag or NONE if not found
         * @param tag the tag
         * @return SeverityType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): SeverityType {
            return tagMap[tag] ?: NONE
        }
    }
}
