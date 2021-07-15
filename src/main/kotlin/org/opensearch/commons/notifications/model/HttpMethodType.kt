package org.opensearch.commons.notifications.model

import org.opensearch.commons.utils.EnumParser

enum class HttpMethodType(val tag: String) {
    POST("POST") {
        override fun toString(): String {
            return tag
        }
    },
    PUT("PUT") {
        override fun toString(): String {
            return tag
        }
    },
    PATCH("PATCH") {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get HttpMethodType from tag or POST if not found
         * @param tag the tag
         * @return MethodType corresponding to tag. POST if invalid tag.
         */
        fun fromTagOrDefault(tag: String): HttpMethodType {
            return tagMap[tag] ?: POST
        }
    }
}
