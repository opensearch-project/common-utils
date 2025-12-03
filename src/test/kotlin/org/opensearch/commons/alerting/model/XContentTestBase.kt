package org.opensearch.commons.alerting.model

import org.opensearch.common.settings.Settings
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.search.SearchModule

interface XContentTestBase {
    fun builder(): XContentBuilder = XContentBuilder.builder(XContentType.JSON.xContent())

    fun parser(xc: String): XContentParser {
        val parser = XContentType.JSON.xContent().createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, xc)
        parser.nextToken()
        return parser
    }

    fun xContentRegistry(): NamedXContentRegistry =
        NamedXContentRegistry(
            listOf(SearchInput.XCONTENT_REGISTRY) + SearchModule(Settings.EMPTY, emptyList()).namedXContents,
        )
}
