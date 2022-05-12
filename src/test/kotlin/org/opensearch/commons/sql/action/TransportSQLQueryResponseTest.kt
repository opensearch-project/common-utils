/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.sql.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject

internal class TransportSQLQueryResponseTest {
    @Test
    fun `Create response serialize and deserialize transport object should be equal`() {
        val queryResponse = TransportSQLQueryResponse("response string")
        val recreatedObject = recreateObject(queryResponse) { TransportSQLQueryResponse(it) }
        assertEquals(queryResponse.queryResponse, recreatedObject.queryResponse)
    }
}
