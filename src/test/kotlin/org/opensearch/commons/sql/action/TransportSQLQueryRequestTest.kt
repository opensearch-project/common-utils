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

internal class TransportSQLQueryRequestTest {

    private fun assertGetRequestEquals(
        expected: TransportSQLQueryRequest,
        actual: TransportSQLQueryRequest
    ) {
        assertEquals(expected.query, actual.query)
        assertEquals(expected.threadContext, actual.threadContext)
        assertNull(actual.validate())
    }

    @Test
    fun `Send request serialize and deserialize transport object should be equal SQL`() {
        val query = "SELECT * FROM account;"
        val request = TransportSQLQueryRequest(query, "sample-thread-context")
        val recreatedObject = recreateObject(request) { TransportSQLQueryRequest(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send query request validate return exception if query is empty`() {
        val query = ""
        val request = TransportSQLQueryRequest(query, "sample-thread-context")
        val recreatedObject = recreateObject(request) { TransportSQLQueryRequest(it) }
        assertNotNull(recreatedObject.validate())
    }
}
