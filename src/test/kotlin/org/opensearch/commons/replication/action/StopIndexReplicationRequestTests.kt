/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.replication.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject

internal class StopIndexReplicationRequestTests {
    @Test
    fun `Stop Replication request serialize and deserialize transport object should be equal`() {
        val index = "test-idx"
        val request = StopIndexReplicationRequest(index)
        val recreatedRequest = recreateObject(request) { StopIndexReplicationRequest(it) }
        assertNotNull(recreatedRequest)
        assertEquals(request.indexName, recreatedRequest.indexName)
        assertNull(recreatedRequest.validate())
    }
}
