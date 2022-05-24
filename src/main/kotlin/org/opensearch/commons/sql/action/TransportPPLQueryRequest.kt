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
import org.opensearch.commons.sql.model.Style
import java.io.IOException

/**
 * Action Request to send ppl query.
 */
class TransportPPLQueryRequest : ActionRequest, ToXContentObject {
    val pplQuery: String
    val path: String
    var format: String
    var sanitize: Boolean?
    var style: Style?
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
        path: String,
        format: String = "",
        sanitize: Boolean? = true,
        style: Style? = Style.COMPACT,
        threadContext: String?
    ) {
        this.pplQuery = query
        this.path = path
        this.format = format
        this.sanitize = sanitize
        this.style = style
        this.threadContext = threadContext
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        pplQuery = input.readString()
        path = input.readString()
        format = input.readString()
        sanitize = input.readOptionalBoolean()
        style = input.readEnum(Style::class.java)
        threadContext = input.readOptionalString()
    }

    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(pplQuery)
        output.writeString(path)
        output.writeOptionalString(format)
        output.writeOptionalBoolean(sanitize)
        output.writeEnum(style)
        output.writeOptionalString(threadContext)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder? {
        return null
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (pplQuery.isEmpty()) {
            validationException = ValidateActions.addValidationError("query is empty", validationException)
        }
        return validationException
    }
}
