/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateUrl
import org.opensearch.core.common.Strings
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing MicrosoftTeams channel.
 */
data class MicrosoftTeams(
    val url: String
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(url)) { "URL is null or empty" }
        validateUrl(url)
    }

    companion object {
        private val log by logger(MicrosoftTeams::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { MicrosoftTeams(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): MicrosoftTeams {
            var url: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    URL_TAG -> url = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing MicrosoftTeams destination")
                    }
                }
            }
            url ?: throw IllegalArgumentException("$URL_TAG field absent")
            return MicrosoftTeams(url)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        url = input.readString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(url)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(URL_TAG, url)
            .endObject()
    }
}
