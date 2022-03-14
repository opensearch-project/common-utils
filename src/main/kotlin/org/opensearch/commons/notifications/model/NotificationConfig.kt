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
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.DESCRIPTION_TAG
import org.opensearch.commons.notifications.NotificationConstants.IS_ENABLED_TAG
import org.opensearch.commons.notifications.NotificationConstants.NAME_TAG
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.createConfigData
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.getReaderForConfigType
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.validateConfigData
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * Data class representing Notification config.
 */
data class NotificationConfig(
    val name: String,
    val description: String,
    val configType: ConfigType,
    val configData: BaseConfigData?,
    val isEnabled: Boolean = true
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        if (!validateConfigData(configType, configData)) {
            throw IllegalArgumentException("ConfigType: $configType and data doesn't match")
        }
        if (configType === ConfigType.NONE) {
            log.info("Some config field not recognized")
        }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationConfig(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @Suppress("ComplexMethod")
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationConfig {
            var name: String? = null
            var description = ""
            var configType: ConfigType? = null
            var isEnabled = true
            var configData: BaseConfigData? = null
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    DESCRIPTION_TAG -> description = parser.text()
                    CONFIG_TYPE_TAG -> configType = ConfigType.fromTagOrDefault(parser.text())
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    else -> {
                        val configTypeForTag = ConfigType.fromTagOrDefault(fieldName)
                        if (configTypeForTag != ConfigType.NONE && configData == null) {
                            configData = createConfigData(configTypeForTag, parser)
                        } else {
                            parser.skipChildren()
                            log.info("Unexpected field: $fieldName, while parsing configuration")
                        }
                    }
                }
            }
            name ?: throw IllegalArgumentException("$NAME_TAG field absent")
            configType ?: throw IllegalArgumentException("$CONFIG_TYPE_TAG field absent")
            return NotificationConfig(
                name,
                description,
                configType,
                configData,
                isEnabled
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(NAME_TAG, name)
            .field(DESCRIPTION_TAG, description)
            .field(CONFIG_TYPE_TAG, configType.tag)
            .field(IS_ENABLED_TAG, isEnabled)
            .fieldIfNotNull(configType.tag, configData)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        name = input.readString(),
        description = input.readString(),
        configType = input.readEnum(ConfigType::class.java),
        isEnabled = input.readBoolean(),
        configData = input.readOptionalWriteable(getReaderForConfigType(input.readEnum(ConfigType::class.java)))
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(name)
        output.writeString(description)
        output.writeEnum(configType)
        output.writeBoolean(isEnabled)
        // Reading config types multiple times in constructor
        output.writeEnum(configType)
        output.writeOptionalWriteable(configData)
    }
}
