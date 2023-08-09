/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.REFERENCE_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.SEVERITY_TAG
import org.opensearch.commons.notifications.NotificationConstants.TAGS_TAG
import org.opensearch.commons.notifications.NotificationConstants.TITLE_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.core.common.Strings
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

/**
 * Data class representing Notification event source.
 */
data class EventSource(
    val title: String,
    val referenceId: String,
    val severity: SeverityType = SeverityType.INFO,
    val tags: List<String> = listOf()
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(title)) { "name is null or empty" }
    }

    companion object {
        private val log by logger(EventSource::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { EventSource(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): EventSource {
            var title: String? = null
            var referenceId: String? = null
            var severity: SeverityType = SeverityType.INFO
            var tags: List<String> = emptyList()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    TITLE_TAG -> title = parser.text()
                    REFERENCE_ID_TAG -> referenceId = parser.text()
                    SEVERITY_TAG -> severity = SeverityType.fromTagOrDefault(parser.text())
                    TAGS_TAG -> tags = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing EventSource")
                    }
                }
            }
            title ?: throw IllegalArgumentException("$TITLE_TAG field absent")
            referenceId ?: throw IllegalArgumentException("$REFERENCE_ID_TAG field absent")

            return EventSource(
                title,
                referenceId,
                severity,
                tags
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(TITLE_TAG, title)
            .field(REFERENCE_ID_TAG, referenceId)
            .field(SEVERITY_TAG, severity.tag)
            .field(TAGS_TAG, tags)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        title = input.readString(),
        referenceId = input.readString(),
        severity = input.readEnum(SeverityType::class.java),
        tags = input.readStringList()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(title)
        output.writeString(referenceId)
        output.writeEnum(severity)
        output.writeStringCollection(tags)
    }
}
