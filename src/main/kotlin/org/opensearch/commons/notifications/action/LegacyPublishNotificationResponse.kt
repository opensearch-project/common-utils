/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.commons.notifications.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.commons.destination.response.LegacyDestinationResponse
import java.io.IOException

/**
 * Action Response for legacy publish notification.
 */
class LegacyPublishNotificationResponse : BaseResponse {
    val destinationResponse: LegacyDestinationResponse

    companion object {
        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { LegacyPublishNotificationResponse(it) }
    }

    /**
     * constructor for creating the class
     * @param destinationResponse the response of the published notification
     */
    constructor(destinationResponse: LegacyDestinationResponse) {
        this.destinationResponse = destinationResponse
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        destinationResponse = LegacyDestinationResponse(input)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        destinationResponse.writeTo(output)
    }

    // This class is only used across transport wire and does not need to implement toXContent
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        throw IllegalStateException("Legacy notification response is not intended for REST or persistence and does not support XContent.")
    }
}
