/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TAG
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException
import java.time.Instant

/**
 * Data class representing Notification config.
 */
data class NotificationConfigInfo(
    val configId: String,
    val lastUpdatedTime: Instant,
    val createdTime: Instant,
    val notificationConfig: NotificationConfig
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(configId)) { "config id is null or empty" }
    }

    companion object {
        private val log by logger(NotificationConfigInfo::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationConfigInfo(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationConfigInfo {
            var configId: String? = null
            var lastUpdatedTime: Instant? = null
            var createdTime: Instant? = null
            var notificationConfig: NotificationConfig? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_ID_TAG -> configId = parser.text()
                    UPDATED_TIME_TAG -> lastUpdatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_TAG -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    CONFIG_TAG -> notificationConfig = NotificationConfig.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing configuration")
                    }
                }
            }
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            lastUpdatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_TAG field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_TAG field absent")
            notificationConfig ?: throw IllegalArgumentException("$CONFIG_TAG field absent")
            return NotificationConfigInfo(
                configId,
                lastUpdatedTime,
                createdTime,
                notificationConfig
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        configId = input.readString(),
        lastUpdatedTime = input.readInstant(),
        createdTime = input.readInstant(),
        notificationConfig = NotificationConfig.reader.read(input)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(configId)
        output.writeInstant(lastUpdatedTime)
        output.writeInstant(createdTime)
        notificationConfig.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_TAG, configId)
            .field(UPDATED_TIME_TAG, lastUpdatedTime.toEpochMilli())
            .field(CREATED_TIME_TAG, createdTime.toEpochMilli())
            .field(CONFIG_TAG, notificationConfig)
            .endObject()
    }
}
