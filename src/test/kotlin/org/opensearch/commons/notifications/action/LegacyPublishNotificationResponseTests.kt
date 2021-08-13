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

package org.opensearch.commons.notifications.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.destination.response.LegacyDestinationResponse
import org.opensearch.commons.utils.recreateObject

internal class LegacyPublishNotificationResponseTests {

    @Test
    fun `Create response serialize and deserialize transport object should be equal`() {
        val res = LegacyDestinationResponse.Builder().withStatusCode(200).withResponseContent("Hello world").build()
        val configResponse = LegacyPublishNotificationResponse(res)
        val recreatedObject = recreateObject(configResponse) { LegacyPublishNotificationResponse(it) }
        assertEquals(configResponse.destinationResponse.statusCode, recreatedObject.destinationResponse.statusCode)
        assertEquals(configResponse.destinationResponse.responseContent, recreatedObject.destinationResponse.responseContent)
    }
}
