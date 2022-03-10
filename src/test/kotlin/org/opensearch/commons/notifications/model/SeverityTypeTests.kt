/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.SeverityType.Companion.enumParser
import org.opensearch.commons.notifications.model.SeverityType.Companion.fromTagOrDefault

internal class SeverityTypeTests {

    @Test
    fun `toString should return tag`() {
        SeverityType.values().forEach {
            assertEquals(it.tag, it.toString())
        }
    }

    @Test
    fun `fromTagOrDefault should return corresponding enum`() {
        SeverityType.values().forEach {
            assertEquals(it, fromTagOrDefault(it.tag))
        }
    }

    @Test
    fun `EnumParser fromTagOrDefault should return corresponding enum`() {
        SeverityType.values().forEach {
            assertEquals(it, enumParser.fromTagOrDefault(it.tag))
        }
    }
}
