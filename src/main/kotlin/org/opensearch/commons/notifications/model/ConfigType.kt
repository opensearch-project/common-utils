/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.commons.utils.EnumParser

/**
 * Enum for Notification config type
 */
enum class ConfigType(
    val tag: String,
) {
    NONE("none") {
        override fun toString(): String = tag
    },
    SLACK("slack") {
        override fun toString(): String = tag
    },
    CHIME("chime") {
        override fun toString(): String = tag
    },
    WEBHOOK("webhook") {
        override fun toString(): String = tag
    },
    EMAIL("email") {
        override fun toString(): String = tag
    },
    SNS("sns") {
        override fun toString(): String = tag
    },
    SES_ACCOUNT("ses_account") {
        override fun toString(): String = tag
    },
    SMTP_ACCOUNT("smtp_account") {
        override fun toString(): String = tag
    },
    EMAIL_GROUP("email_group") {
        override fun toString(): String = tag
    },
    MICROSOFT_TEAMS("microsoft_teams") {
        override fun toString(): String = tag
    },
    MATTERMOST("mattermost") {
        override fun toString(): String = tag
    }, ;

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get ConfigType from tag or NONE if not found
         * @param tag the tag
         * @return ConfigType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): ConfigType = tagMap[tag] ?: NONE
    }
}
