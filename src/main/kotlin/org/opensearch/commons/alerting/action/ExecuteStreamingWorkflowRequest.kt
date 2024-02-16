/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.commons.alerting.model.StreamingIndex
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

class ExecuteStreamingWorkflowRequest : ActionRequest {
    var workflowId: String
    var indices: List<StreamingIndex>

    constructor(workflowId: String, indices: List<StreamingIndex>) : super() {
        this.workflowId = workflowId
        this.indices = indices
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        workflowId = sin.readString(),
        indices = sin.readList(::StreamingIndex)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(workflowId)
        out.writeCollection(indices)
    }

    companion object {
        const val WORKFLOW_ID_FIELD = "workflowId"
        const val INDICES_FIELD = "indices"

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): ExecuteStreamingWorkflowRequest {
            var workflowId: String? = null
            var indices: MutableList<StreamingIndex> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    WORKFLOW_ID_FIELD -> workflowId = xcp.text()
                    INDICES_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val index = StreamingIndex.parse(xcp)
                            indices.add(index)
                        }
                    }

                    else -> {
                        xcp.skipChildren()
                    }
                }
            }

            return ExecuteStreamingWorkflowRequest(
                requireNotNull(workflowId) { "workflowId is null" },
                indices
            )
        }
    }
}
