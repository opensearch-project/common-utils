/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.COMPACT_TAG
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * Action Request for getting notification plugin features.
 */
class GetPluginFeaturesRequest : ActionRequest, ToXContentObject {
    val compact: Boolean // Dummy request parameter for transport request

    companion object {
        private val log by logger(GetPluginFeaturesRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetPluginFeaturesRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetPluginFeaturesRequest {
            var compact = false

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    COMPACT_TAG -> compact = parser.booleanValue()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing GetPluginFeaturesRequest")
                    }
                }
            }
            return GetPluginFeaturesRequest(compact)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(COMPACT_TAG, compact)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param compact Dummy request parameter for transport request
     */
    constructor(compact: Boolean = false) {
        this.compact = compact
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        compact = input.readBoolean()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeBoolean(compact)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
