/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.opensearch.commons.notifications.NotificationConstants.ATTACHMENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.HTML_DESCRIPTION_TAG
import org.opensearch.commons.notifications.NotificationConstants.TEXT_DESCRIPTION_TAG
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
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
 * Data class for storing channel message.
 */
data class ChannelMessage(
    val textDescription: String,
    val htmlDescription: String?,
    val attachment: Attachment?
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(textDescription)) { "text message part is null or empty" }
    }

    companion object {
        private val log by logger(ChannelMessage::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { ChannelMessage(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): ChannelMessage {
            var textDescription: String? = null
            var htmlDescription: String? = null
            var attachment: Attachment? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )

            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    TEXT_DESCRIPTION_TAG -> textDescription = parser.text()
                    HTML_DESCRIPTION_TAG -> htmlDescription = parser.textOrNull()
                    ATTACHMENT_TAG -> attachment = Attachment.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Skipping Unknown field $fieldName")
                    }
                }
            }

            textDescription ?: throw IllegalArgumentException("$TEXT_DESCRIPTION_TAG not present")
            return ChannelMessage(
                textDescription,
                htmlDescription,
                attachment
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        textDescription = input.readString(),
        htmlDescription = input.readOptionalString(),
        attachment = input.readOptionalWriteable(Attachment.reader)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(textDescription)
        output.writeOptionalString(htmlDescription)
        output.writeOptionalWriteable(attachment)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(TEXT_DESCRIPTION_TAG, textDescription)
            .fieldIfNotNull(HTML_DESCRIPTION_TAG, htmlDescription)
            .fieldIfNotNull(ATTACHMENT_TAG, attachment)
            .endObject()
    }
}
