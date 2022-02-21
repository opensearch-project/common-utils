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
import org.opensearch.commons.notifications.model.NotificationEventSearchResult
import java.io.IOException

/**
 * Action Response for getting notification event.
 */
class GetNotificationEventResponse : BaseResponse {
    val searchResult: NotificationEventSearchResult

    companion object {

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetNotificationEventResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetNotificationEventResponse {
            return GetNotificationEventResponse(NotificationEventSearchResult(parser))
        }
    }

    /**
     * constructor for creating the class
     * @param searchResult the notification event list
     */
    constructor(searchResult: NotificationEventSearchResult) {
        this.searchResult = searchResult
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        searchResult = NotificationEventSearchResult(input)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        searchResult.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return searchResult.toXContent(builder, params)
    }
}
