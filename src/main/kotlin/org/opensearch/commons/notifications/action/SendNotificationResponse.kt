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
import org.opensearch.commons.notifications.model.NotificationEvent
import java.io.IOException

/**
 * Action Response for send notification.
 */
class SendNotificationResponse : BaseResponse {
    val notificationEvent: NotificationEvent

    companion object {

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
            return SendNotificationResponse(NotificationEvent.parse(parser))
        }
    }

    /**
     * constructor for creating the class
     * @param notificationEvent the id of the created notification configuration
     */
    constructor(notificationEvent: NotificationEvent) {
        this.notificationEvent = notificationEvent
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        notificationEvent = NotificationEvent(input)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        notificationEvent.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return notificationEvent.toXContent(builder, params)
    }
}
