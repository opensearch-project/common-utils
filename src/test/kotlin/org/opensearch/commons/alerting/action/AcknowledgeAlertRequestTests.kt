/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.action.support.WriteRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput

class AcknowledgeAlertRequestTests {

    @Test
    fun `test acknowledge alert request`() {
        val req = AcknowledgeAlertRequest("1234", mutableListOf("1", "2", "3", "4"), WriteRequest.RefreshPolicy.IMMEDIATE)
        assertNotNull(req)
        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = AcknowledgeAlertRequest(sin)
        assertEquals("1234", newReq.monitorId)
        assertEquals(4, newReq.alertIds.size)
        assertEquals(WriteRequest.RefreshPolicy.IMMEDIATE, newReq.refreshPolicy)
    }
}
