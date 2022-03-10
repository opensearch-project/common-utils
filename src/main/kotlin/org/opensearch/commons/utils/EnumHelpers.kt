/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.utils

import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.util.EnumSet

inline fun <reified E : Enum<E>> XContentParser.enumSet(enumParser: EnumParser<E>): EnumSet<E> {
    val retSet: EnumSet<E> = EnumSet.noneOf(E::class.java)
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retSet.add(enumParser.fromTagOrDefault(text()))
    }
    return retSet
}

inline fun <reified E : Enum<E>> enumReader(enumClass: Class<E>): Writeable.Reader<E> {
    return Writeable.Reader<E> {
        it.readEnum(enumClass)
    }
}

@Suppress("UnusedPrivateMember")
inline fun <reified E : Enum<E>> enumWriter(ignore: Class<E>): Writeable.Writer<E> {
    return Writeable.Writer<E> { streamOutput: StreamOutput, value: E ->
        streamOutput.writeEnum(value)
    }
}
