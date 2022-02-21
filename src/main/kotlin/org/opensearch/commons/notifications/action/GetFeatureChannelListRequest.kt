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
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_TAG
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * This request is plugin-only call. i.e. REST interface is not exposed.
 * Also the library will remove the user context while making this call
 * so that user making this call need not have to set permission to this API.
 * Hence the request also contains tenant info for space isolation.
 */
class GetFeatureChannelListRequest : ActionRequest, ToXContentObject {
    val feature: Feature

    companion object {
        private val log by logger(GetFeatureChannelListRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetFeatureChannelListRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetFeatureChannelListRequest {
            var feature: Feature? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    FEATURE_TAG -> feature = Feature.fromTagOrDefault(parser.text())
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing GetFeatureChannelListRequest")
                    }
                }
            }
            feature ?: throw IllegalArgumentException("$FEATURE_TAG field absent")
            return GetFeatureChannelListRequest(feature)
        }
    }

    /**
     * constructor for creating the class
     * @param feature the caller plugin feature
     */
    constructor(feature: Feature) {
        this.feature = feature
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        feature = input.readEnum(Feature::class.java)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeEnum(feature)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(FEATURE_TAG, feature)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
