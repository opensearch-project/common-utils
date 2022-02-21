/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.ROLE_ARN_TAG
import org.opensearch.commons.notifications.NotificationConstants.TOPIC_ARN_TAG
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateIamRoleArn
import java.io.IOException
import java.util.regex.Pattern

/**
 * SNS notification data model
 */
data class Sns(val topicArn: String, val roleArn: String?) : BaseConfigData {

    init {
        require(SNS_ARN_REGEX.matcher(topicArn).find()) { "Invalid AWS SNS topic ARN: $topicArn" }
        if (roleArn != null) {
            validateIamRoleArn(roleArn)
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(TOPIC_ARN_TAG, topicArn)
            .fieldIfNotNull(ROLE_ARN_TAG, roleArn)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        topicArn = input.readString(),
        roleArn = input.readOptionalString()
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(topicArn)
        out.writeOptionalString(roleArn)
    }

    companion object {
        private val log by logger(Sns::class.java)

        private val SNS_ARN_REGEX =
            Pattern.compile("^arn:aws(-[^:]+)?:sns:([a-zA-Z0-9-]+):([0-9]{12}):([a-zA-Z0-9-_]+)$")

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Sns(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Sns {
            var topicArn: String? = null
            var roleArn: String? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    TOPIC_ARN_TAG -> topicArn = xcp.textOrNull()
                    ROLE_ARN_TAG -> roleArn = xcp.textOrNull()
                    else -> {
                        xcp.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SNS destination")
                    }
                }
            }
            topicArn ?: throw IllegalArgumentException("$TOPIC_ARN_TAG field absent")
            return Sns(topicArn, roleArn)
        }
    }
}
