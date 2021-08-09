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
package org.opensearch.commons.notifications.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.ROLE_ARN_FIELD
import org.opensearch.commons.notifications.NotificationConstants.TOPIC_ARN_FIELD
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateIAMRoleArn
import java.io.IOException
import java.util.regex.Pattern

/**
 * SNS notification data model
 */
data class SNS(val topicARN: String, val roleARN: String?) : BaseConfigData {

    init {
        require(SNS_ARN_REGEX.matcher(topicARN).find()) { "Invalid AWS SNS topic ARN: $topicARN" }
        if (roleARN != null) {
            validateIAMRoleArn(roleARN)
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(TOPIC_ARN_FIELD, topicARN)
            .fieldIfNotNull(ROLE_ARN_FIELD, roleARN)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        topicARN = input.readString(),
        roleARN = input.readOptionalString()
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(topicARN)
        out.writeOptionalString(roleARN)
    }

    companion object {
        private val log by logger(SNS::class.java)

        private val SNS_ARN_REGEX =
            Pattern.compile("^arn:aws(-[^:]+)?:sns:([a-zA-Z0-9-]+):([0-9]{12}):([a-zA-Z0-9-_]+)$")

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SNS(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): SNS {
            var topicARN: String? = null
            var roleARN: String? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    TOPIC_ARN_FIELD -> topicARN = xcp.textOrNull()
                    ROLE_ARN_FIELD -> roleARN = xcp.textOrNull()
                    else -> {
                        xcp.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SNS destination")
                    }
                }
            }
            topicARN ?: throw IllegalArgumentException("$TOPIC_ARN_FIELD field absent")
            return SNS(topicARN, roleARN)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): SNS? {
            return if (sin.readBoolean()) {
                SNS(
                    topicARN = sin.readString(),
                    roleARN = sin.readOptionalString()
                )
            } else null
        }
    }
}
