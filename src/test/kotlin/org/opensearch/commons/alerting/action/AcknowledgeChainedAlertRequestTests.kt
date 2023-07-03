/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput

class AcknowledgeChainedAlertRequestTests {

    @Test
    fun `test acknowledge chained alert request`() {
        val req = AcknowledgeChainedAlertRequest("1234", mutableListOf("1", "2", "3", "4"))
        assertNotNull(req)
        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = AcknowledgeChainedAlertRequest(sin)
        assertEquals("1234", newReq.workflowId)
        assertEquals(4, newReq.alertIds.size)
    }
}
