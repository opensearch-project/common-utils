/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import java.io.IOException

/**
 * Action Response for sql query.
 */
class TransportSQLQueryResponse : BaseResponse {
    val queryResponse: String

    companion object {
        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { TransportSQLQueryResponse(it) }
    }

    /**
     * constructor for creating the class
     * @param queryResponse the sql query response
     */
    constructor(
        queryResponse: String,
    ) {
        this.queryResponse = queryResponse
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        queryResponse = input.readString()
    }

    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(queryResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        throw IllegalStateException("Transport sql response is not intended for REST or persistence and does not support XContent.")
    }
}
