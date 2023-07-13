/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

/**
 * Action Response for getting notification configuration.
 */
class GetNotificationConfigResponse : BaseResponse {
    val searchResult: NotificationConfigSearchResult

    companion object {

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetNotificationConfigResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetNotificationConfigResponse {
            return GetNotificationConfigResponse(NotificationConfigSearchResult(parser))
        }
    }

    /**
     * constructor for creating the class
     * @param searchResult the notification configuration list
     */
    constructor(searchResult: NotificationConfigSearchResult) {
        this.searchResult = searchResult
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        searchResult = NotificationConfigSearchResult(input)
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
