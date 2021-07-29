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
import java.io.IOException
import java.util.regex.Pattern

/**
 * SNS notification data model
 */
data class SNS(val topicARN: String, val roleARN: String?) : BaseConfigData {

    init {
        require(SNS_ARN_REGEX.matcher(topicARN).find()) { "Invalid AWS SNS topic ARN: $topicARN" }
        if (roleARN != null) {
            require(IAM_ARN_REGEX.matcher(roleARN).find()) { "Invalid AWS role ARN: $roleARN " }
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject(SNS_TYPE)
            .field(TOPIC_ARN_FIELD, topicARN)
            .field(ROLE_ARN_FIELD, roleARN) // TODO: optional?
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        topicARN = input.readString(),
        roleARN = input.readString()
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(topicARN)
        out.writeOptionalString(roleARN)
    }

    companion object {

        private val SNS_ARN_REGEX =
            Pattern.compile("^arn:aws(-[^:]+)?:sns:([a-zA-Z0-9-]+):([0-9]{12}):([a-zA-Z0-9-_]+)$")
        private val IAM_ARN_REGEX = Pattern.compile("^arn:aws(-[^:]+)?:iam::([0-9]{12}):([a-zA-Z_0-9+=,.@\\-_/]+)$")

        const val TOPIC_ARN_FIELD = "topic_arn"
        const val ROLE_ARN_FIELD = "role_arn"
        const val SNS_TYPE = "sns"

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
            lateinit var topicARN: String
            var roleARN: String? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    TOPIC_ARN_FIELD -> topicARN = xcp.textOrNull()
                    ROLE_ARN_FIELD -> roleARN = xcp.textOrNull()
                    else -> {
                        throw IllegalStateException("Unexpected field: $fieldName, while parsing SNS destination")
                    }
                }
            }
            // if (DestinationType.snsUseIamRole) {
            //     requireNotNull(roleARN) { "SNS Action role_arn is null" }
            // }
            return SNS(requireNotNull(topicARN) { "SNS Action topic_arn is null" }, roleARN)
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
