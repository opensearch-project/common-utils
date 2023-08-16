/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateUrl
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException
<<<<<<< HEAD

/**
 * Data class representing Microsoft Teams channel.
 */

data class MicrosoftTeams(
        val url: String
=======
/**
 * Data class representing Microsoft Teams channel.
 */
data class MicrosoftTeams(
    val url: String
>>>>>>> b13bf3d337f15337a3c6999c5285482a79fd5a5d
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(url)) { "URL is null or empty" }
        validateUrl(url)
    }

    companion object {
        private val log by logger(MicrosoftTeams::class.java)

        /**
<<<<<<< HEAD
         * reader to create instance of class from writable.
=======
         * Reader to create instance of class from writable.
>>>>>>> b13bf3d337f15337a3c6999c5285482a79fd5a5d
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
<<<<<<< HEAD
                    XContentParser.Token.START_OBJECT,
                    parser.currentToken(),
                    parser
=======
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
>>>>>>> b13bf3d337f15337a3c6999c5285482a79fd5a5d
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    URL_TAG -> url = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Microsoft Teams destination")
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
<<<<<<< HEAD
            url = input.readString()
=======
        url = input.readString()
>>>>>>> b13bf3d337f15337a3c6999c5285482a79fd5a5d
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
<<<<<<< HEAD
                .field(URL_TAG, url)
                .endObject()
=======
            .field(URL_TAG, url)
            .endObject()
>>>>>>> b13bf3d337f15337a3c6999c5285482a79fd5a5d
    }
}
