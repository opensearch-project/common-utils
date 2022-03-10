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
import org.opensearch.commons.notifications.model.FeatureChannelList
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class GetFeatureChannelListResponse : BaseResponse {
    val searchResult: FeatureChannelList

    companion object {

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetFeatureChannelListResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetFeatureChannelListResponse {
            return GetFeatureChannelListResponse(FeatureChannelList(parser))
        }
    }

    /**
     * constructor for creating the class
     * @param searchResult the notification configuration list
     */
    constructor(searchResult: FeatureChannelList) {
        this.searchResult = searchResult
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        searchResult = FeatureChannelList(input)
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
