/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import org.opensearch.common.io.stream.InputStreamStreamInput
import org.opensearch.common.io.stream.NamedWriteableAwareStreamInput
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.io.stream.OutputStreamStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

val STRING_READER = Writeable.Reader {
    it.readString()
}

val STRING_WRITER = Writeable.Writer { output: StreamOutput, value: String ->
    output.writeString(value)
}

/**
 * Re create the object from the writeable.
 * This method needs to be inline and reified so that when this is called from
 * doExecute() of transport action, the object may be created from other JVM.
 */
inline fun <reified Request> recreateObject(writeable: Writeable, block: (StreamInput) -> Request): Request {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        OutputStreamStreamOutput(byteArrayOutputStream).use {
            writeable.writeTo(it)
            InputStreamStreamInput(ByteArrayInputStream(byteArrayOutputStream.toByteArray())).use { streamInput ->
                return block(streamInput)
            }
        }
    }
}

/**
 * Re create the object from the writeable. Uses NamedWriteableRegistry in order to build the aggregations.
 * This method needs to be inline and reified so that when this is called from
 * doExecute() of transport action, the object may be created from other JVM.
 */
inline fun <reified Request> recreateObject(writeable: Writeable, namedWriteableRegistry: NamedWriteableRegistry, block: (StreamInput) -> Request): Request {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        OutputStreamStreamOutput(byteArrayOutputStream).use {
            writeable.writeTo(it)
            InputStreamStreamInput(ByteArrayInputStream(byteArrayOutputStream.toByteArray())).use { streamInput ->
                return block(NamedWriteableAwareStreamInput(streamInput, namedWriteableRegistry))
            }
        }
    }
}
