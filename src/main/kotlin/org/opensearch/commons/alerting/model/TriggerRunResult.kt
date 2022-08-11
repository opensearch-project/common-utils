/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import java.io.IOException

abstract class TriggerRunResult(
    open var triggerName: String,
    open var error: Exception? = null
) : Writeable, ToXContent {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field("name", triggerName)

        internalXContent(builder, params)
        val msg = error?.message

        builder.field("error", msg)
            .endObject()
        return builder
    }

    abstract fun internalXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder


    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(triggerName)
        out.writeException(error)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun suppressWarning(map: MutableMap<String?, Any?>?): MutableMap<String, ActionRunResult> {
            return map as MutableMap<String, ActionRunResult>
        }
    }
}