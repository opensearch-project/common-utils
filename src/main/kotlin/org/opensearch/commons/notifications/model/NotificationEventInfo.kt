/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.utils.logger
import java.io.IOException
import java.time.Instant

/**
 * Data class representing Notification event with information.
 */
data class NotificationEventInfo(
    val eventId: String,
    val lastUpdatedTime: Instant,
    val createdTime: Instant,
    val notificationEvent: NotificationEvent
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(eventId)) { "event id is null or empty" }
    }

    companion object {
        private val log by logger(NotificationEventInfo::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationEventInfo(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationEventInfo {
            var eventId: String? = null
            var lastUpdatedTime: Instant? = null
            var createdTime: Instant? = null
            var notificationEvent: NotificationEvent? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EVENT_ID_TAG -> eventId = parser.text()
                    UPDATED_TIME_TAG -> lastUpdatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_TAG -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    EVENT_TAG -> notificationEvent = NotificationEvent.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing event info")
                    }
                }
            }
            eventId ?: throw IllegalArgumentException("$EVENT_ID_TAG field absent")
            lastUpdatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_TAG field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_TAG field absent")
            notificationEvent ?: throw IllegalArgumentException("$EVENT_TAG field absent")
            return NotificationEventInfo(
                eventId,
                lastUpdatedTime,
                createdTime,
                notificationEvent
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EVENT_ID_TAG, eventId)
            .field(UPDATED_TIME_TAG, lastUpdatedTime.toEpochMilli())
            .field(CREATED_TIME_TAG, createdTime.toEpochMilli())
            .field(EVENT_TAG, notificationEvent)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        eventId = input.readString(),
        lastUpdatedTime = input.readInstant(),
        createdTime = input.readInstant(),
        notificationEvent = NotificationEvent.reader.read(input)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(eventId)
        output.writeInstant(lastUpdatedTime)
        output.writeInstant(createdTime)
        notificationEvent.writeTo(output)
    }
}
