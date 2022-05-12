/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql.model

import org.opensearch.commons.utils.EnumParser

/**
 * Enum for Notification config type
 */
enum class QueryType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    SQL("ppl") {
        override fun toString(): String {
            return tag
        }
    },
    PPL("sql") {
        override fun toString(): String {
            return tag
        }
    };
    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get QueryType from tag or NONE if not found
         * @param tag the tag
         * @return QueryType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): QueryType {
            return tagMap[tag] ?: NONE
        }
    }
}
