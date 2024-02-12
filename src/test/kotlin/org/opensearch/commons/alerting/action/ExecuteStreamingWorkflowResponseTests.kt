/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.rest.RestStatus

class ExecuteStreamingWorkflowResponseTests {

    @Test
    fun `test execute streaming workflow request`() {

        val req = ExecuteStreamingWorkflowResponse(RestStatus.OK)
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = ExecuteStreamingWorkflowResponse(sin)
        Assertions.assertEquals(RestStatus.OK, newReq.getStatus())
    }
}
