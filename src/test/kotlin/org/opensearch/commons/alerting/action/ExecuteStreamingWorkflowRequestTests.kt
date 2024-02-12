/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.StreamingIndex
import org.opensearch.commons.alerting.randomIdDocPair
import org.opensearch.core.common.io.stream.StreamInput

class ExecuteStreamingWorkflowRequestTests {

    @Test
    fun `test execute streaming workflow request`() {

        val req = ExecuteStreamingWorkflowRequest("1234", listOf(StreamingIndex("index123", listOf(randomIdDocPair("docId123")))))
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = ExecuteStreamingWorkflowRequest(sin)
        Assertions.assertEquals("1234", newReq.workflowId)
        Assertions.assertEquals(1, newReq.indices.size)
        Assertions.assertEquals("index123", newReq.indices[0].index)
        Assertions.assertEquals(1, newReq.indices[0].idDocPairs.size)
        Assertions.assertEquals("docId123", newReq.indices[0].idDocPairs[0].docId)
    }
}
