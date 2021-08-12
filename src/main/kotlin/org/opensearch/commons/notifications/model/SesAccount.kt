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

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.FROM_ADDRESS_TAG
import org.opensearch.commons.notifications.NotificationConstants.REGION_TAG
import org.opensearch.commons.notifications.NotificationConstants.ROLE_ARN_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateEmail
import org.opensearch.commons.utils.validateIamRoleArn
import java.io.IOException

/**
 * Data class representing SES account channel.
 */
data class SesAccount(
    val awsRegion: String,
    val roleArn: String?,
    val fromAddress: String
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(awsRegion)) { "awsRegion is null or empty" }
        validateEmail(fromAddress)
        if (roleArn != null) {
            validateIamRoleArn(roleArn)
        }
    }

    companion object {
        private val log by logger(SesAccount::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SesAccount(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SesAccount {
            var awsRegion: String? = null
            var roleArn: String? = null
            var fromAddress: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    REGION_TAG -> awsRegion = parser.text()
                    ROLE_ARN_TAG -> roleArn = parser.text()
                    FROM_ADDRESS_TAG -> fromAddress = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SesAccount")
                    }
                }
            }
            awsRegion ?: throw IllegalArgumentException("$REGION_TAG field absent")
            fromAddress ?: throw IllegalArgumentException("$FROM_ADDRESS_TAG field absent")
            return SesAccount(
                awsRegion,
                roleArn,
                fromAddress
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REGION_TAG, awsRegion)
            .field(ROLE_ARN_TAG, roleArn)
            .field(FROM_ADDRESS_TAG, fromAddress)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        awsRegion = input.readString(),
        roleArn = input.readOptionalString(),
        fromAddress = input.readString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(out: StreamOutput) {
        out.writeString(awsRegion)
        out.writeOptionalString(roleArn)
        out.writeString(fromAddress)
    }
}
