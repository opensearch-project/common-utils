/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.EVENT_LIST_TAG
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.objectList
import java.io.IOException

/**
 * Action Response for send notification.
 */
class SendNotificationResponse : BaseResponse {
    val notificationEvents: List<NotificationEvent>

    companion object {
        private val log by logger(SendNotificationResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SendNotificationResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SendNotificationResponse {
            var notificationEvents: List<NotificationEvent>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EVENT_LIST_TAG -> notificationEvents = parser.objectList { NotificationEvent.parse(it) }
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SendNotificationResponse")
                    }
                }
            }

            notificationEvents ?: throw IllegalArgumentException("$EVENT_LIST_TAG field absent")
            return SendNotificationResponse(notificationEvents)
        }
    }

    /**
     * constructor for creating the class
     * @param notificationEvent the id of the created notification configuration
     */
    constructor(notificationEvents: List<NotificationEvent>) {
        this.notificationEvents = notificationEvents
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        notificationEvents = input.readList(NotificationEvent.reader)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeList(notificationEvents)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(EVENT_LIST_TAG, notificationEvents)
            .endObject()
    }
}
