/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.ALLOWED_CONFIG_TYPE_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.PLUGIN_FEATURES_TAG
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import java.io.IOException

/**
 * Action Response for getting notification plugin features.
 */
class GetPluginFeaturesResponse : BaseResponse {
    val allowedConfigTypeList: List<String>
    val pluginFeatures: Map<String, String>

    companion object {
        private val log by logger(GetPluginFeaturesResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetPluginFeaturesResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetPluginFeaturesResponse {
            var allowedConfigTypeList: List<String>? = null
            var pluginFeatures: Map<String, String>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    ALLOWED_CONFIG_TYPE_LIST_TAG -> allowedConfigTypeList = parser.stringList()
                    PLUGIN_FEATURES_TAG -> pluginFeatures = parser.mapStrings()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing DeleteNotificationConfigResponse")
                    }
                }
            }
            allowedConfigTypeList ?: throw IllegalArgumentException("$ALLOWED_CONFIG_TYPE_LIST_TAG field absent")
            pluginFeatures ?: throw IllegalArgumentException("$PLUGIN_FEATURES_TAG field absent")
            return GetPluginFeaturesResponse(allowedConfigTypeList, pluginFeatures)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(ALLOWED_CONFIG_TYPE_LIST_TAG, allowedConfigTypeList)
            .field(PLUGIN_FEATURES_TAG, pluginFeatures)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param allowedConfigTypeList the list of config types supported by plugin
     * @param pluginFeatures the map of plugin features supported to its value
     */
    constructor(
        allowedConfigTypeList: List<String>,
        pluginFeatures: Map<String, String>
    ) {
        this.allowedConfigTypeList = allowedConfigTypeList
        this.pluginFeatures = pluginFeatures
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        allowedConfigTypeList = input.readStringList()
        pluginFeatures = input.readMap(STRING_READER, STRING_READER)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeStringCollection(allowedConfigTypeList)
        output.writeMap(pluginFeatures, STRING_WRITER, STRING_WRITER)
    }
}
