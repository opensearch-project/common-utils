/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import org.opensearch.commons.notifications.NotificationConstants.HEADER_PARAMS_TAG
import org.opensearch.commons.notifications.NotificationConstants.METHOD_TAG
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateUrl
import java.io.IOException

/**
 * Data class representing Webhook channel.
 */
data class Webhook(
    val url: String,
    val headerParams: Map<String, String> = mapOf(),
    val method: HttpMethodType = HttpMethodType.POST
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(url)) { "URL is null or empty" }
        validateUrl(url)
    }

    companion object {
        private val log by logger(Webhook::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Webhook(it) }

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
        fun parse(parser: XContentParser): Webhook {
            var url: String? = null
            var headerParams: Map<String, String> = mapOf()
            var method = HttpMethodType.POST

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
                    HEADER_PARAMS_TAG -> headerParams = parser.mapStrings()
                    METHOD_TAG -> method = HttpMethodType.fromTagOrDefault(parser.text())
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Webhook destination")
                    }
                }
            }
            url ?: throw IllegalArgumentException("$URL_TAG field absent")
            return Webhook(url, headerParams, method)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(URL_TAG, url)
            .field(HEADER_PARAMS_TAG, headerParams)
            .field(METHOD_TAG, method.tag)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        url = input.readString(),
        headerParams = input.readMap(STRING_READER, STRING_READER),
        method = input.readEnum(HttpMethodType::class.java)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(url)
        output.writeMap(headerParams, STRING_WRITER, STRING_WRITER)
        output.writeEnum(method)
    }
}
