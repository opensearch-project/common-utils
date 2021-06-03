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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.commons.notifications.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.FILE_CONTENT_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.FILE_DATA_TAG
import org.opensearch.commons.notifications.NotificationConstants.FILE_ENCODING_TAG
import org.opensearch.commons.notifications.NotificationConstants.FILE_NAME_TAG
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger

/**
 * Data class for storing attachment of channel message.
 */
data class Attachment(
    val fileName: String,
    val fileEncoding: String,
    val fileData: String,
    val fileContentType: String?
) : BaseModel {
    companion object {
        private val log by logger(Attachment::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Attachment(it) }

        /**
         * Parse the data from parser and create Attachment object
         * @param parser data referenced at parser
         * @return created Attachment object
         */
        fun parse(parser: XContentParser): Attachment {
            var fileName: String? = null
            var fileEncoding: String? = null
            var fileData: String? = null
            var fileContentType: String? = null
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val dataType = parser.currentName()
                parser.nextToken()
                when (dataType) {
                    FILE_NAME_TAG -> fileName = parser.text()
                    FILE_ENCODING_TAG -> fileEncoding = parser.text()
                    FILE_DATA_TAG -> fileData = parser.text()
                    FILE_CONTENT_TYPE_TAG -> fileContentType = parser.textOrNull()
                    else -> {
                        parser.skipChildren()
                        log.info("Skipping Unknown field $dataType")
                    }
                }
            }
            fileName ?: throw IllegalArgumentException("attachment:fileName not present")
            fileEncoding ?: throw IllegalArgumentException("attachment:fileEncoding not present")
            fileData ?: throw IllegalArgumentException("attachment:fileData not present")
            return Attachment(fileName, fileEncoding, fileData, fileContentType)
        }
    }

    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(FILE_NAME_TAG, fileName)
            .field(FILE_ENCODING_TAG, fileEncoding)
            .field(FILE_DATA_TAG, fileData)
            .fieldIfNotNull(FILE_CONTENT_TYPE_TAG, fileContentType)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        fileName = input.readString(),
        fileEncoding = input.readString(),
        fileData = input.readString(),
        fileContentType = input.readOptionalString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(fileName)
        output.writeString(fileEncoding)
        output.writeString(fileData)
        output.writeOptionalString(fileContentType)
    }
}
