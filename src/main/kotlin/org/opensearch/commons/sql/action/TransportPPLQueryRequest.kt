/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import java.io.IOException

/**
 * Action Request to send ppl query.
 */
class TransportPPLQueryRequest : ActionRequest, ToXContentObject {
    val query: String
    val threadContext: String?

    companion object {
        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { TransportPPLQueryRequest(it) }
    }

    /**
     * constructor for creating the class
     * @param query the ppl query string
     * @param threadContext the user info thread context
     */
    constructor(
        query: String,
        threadContext: String?
    ) {
        this.query = query
        this.threadContext = threadContext
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        query = input.readString()
        threadContext = input.readOptionalString()
    }

    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(query)
        output.writeOptionalString(threadContext)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        throw IllegalStateException("Transport ppl request is not intended for REST or persistence and does not support XContent.")
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (query.isEmpty()) {
            validationException = ValidateActions.addValidationError("query is empty", validationException)
        }
        return validationException
    }
}
