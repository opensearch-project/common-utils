/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.commons.destination.message.LegacyBaseMessage
import org.opensearch.commons.destination.message.LegacyChimeMessage
import org.opensearch.commons.destination.message.LegacyCustomWebhookMessage
import org.opensearch.commons.destination.message.LegacyDestinationType
import org.opensearch.commons.destination.message.LegacyEmailMessage
import org.opensearch.commons.destination.message.LegacySlackMessage
import java.io.IOException

/**
 * Action Request to publish notification. This is a legacy implementation.
 * This should not be used going forward, instead use [SendNotificationRequest].
 */
class LegacyPublishNotificationRequest : ActionRequest {
    val baseMessage: LegacyBaseMessage

    companion object {
        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { LegacyPublishNotificationRequest(it) }
    }

    /**
     * constructor for creating the class
     * @param baseMessage the base message to send
     */
    constructor(
        baseMessage: LegacyBaseMessage
    ) {
        this.baseMessage = baseMessage
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        baseMessage = when (requireNotNull(input.readEnum(LegacyDestinationType::class.java)) { "Destination type cannot be null" }) {
            LegacyDestinationType.LEGACY_CHIME -> LegacyChimeMessage(input)
            LegacyDestinationType.LEGACY_CUSTOM_WEBHOOK -> LegacyCustomWebhookMessage(input)
            LegacyDestinationType.LEGACY_SLACK -> LegacySlackMessage(input)
            LegacyDestinationType.LEGACY_EMAIL -> LegacyEmailMessage(input)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeEnum(baseMessage.channelType)
        baseMessage.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? = null
}
