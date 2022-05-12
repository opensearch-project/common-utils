/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject

internal class TransportPPLQueryRequestTest {

    private fun assertGetRequestEquals(
        expected: TransportPPLQueryRequest,
        actual: TransportPPLQueryRequest
    ) {
        assertEquals(expected.query, actual.query)
        assertEquals(expected.threadContext, actual.threadContext)
        assertNull(actual.validate())
    }

    @Test
    fun `Send request serialize and deserialize transport object should be equal PPL`() {
        val query = "search source=accounts"
        val request = TransportPPLQueryRequest(query, "sample-thread-context")
        val recreatedObject = recreateObject(request) { TransportPPLQueryRequest(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send query request validate return exception if query is empty`() {
        val query = ""
        val request = TransportPPLQueryRequest(query, "sample-thread-context")
        val recreatedObject = recreateObject(request) { TransportPPLQueryRequest(it) }
        assertNotNull(recreatedObject.validate())
    }
}
