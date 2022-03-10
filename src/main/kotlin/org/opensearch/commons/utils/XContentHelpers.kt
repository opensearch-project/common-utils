/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.DeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.common.xcontent.XContentType
import org.opensearch.rest.RestRequest

fun StreamInput.createJsonParser(): XContentParser {
    return XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, this)
}

fun RestRequest.contentParserNextToken(): XContentParser {
    val parser = this.contentParser()
    parser.nextToken()
    return parser
}

fun XContentParser.stringList(): List<String> {
    val retList: MutableList<String> = mutableListOf()
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retList.add(text())
    }
    return retList
}

fun XContentBuilder.fieldIfNotNull(name: String, value: Any?): XContentBuilder {
    if (value != null) {
        this.field(name, value)
    }
    return this
}

fun XContentBuilder.objectIfNotNull(name: String, xContentObject: ToXContentObject?): XContentBuilder {
    if (xContentObject != null) {
        this.field(name)
        xContentObject.toXContent(this, ToXContent.EMPTY_PARAMS)
    }
    return this
}

fun <T : ToXContent> XContentParser.objectList(block: (XContentParser) -> T): List<T> {
    val retList: MutableList<T> = mutableListOf()
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retList.add(block(this))
    }
    return retList
}
