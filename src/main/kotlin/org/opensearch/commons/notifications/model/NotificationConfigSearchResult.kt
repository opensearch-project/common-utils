/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_LIST_TAG

/**
 * NotificationConfig search results
 */
class NotificationConfigSearchResult : SearchResults<NotificationConfigInfo> {

    /**
     * single item result constructor
     */
    constructor(objectItem: NotificationConfigInfo) : super(CONFIG_LIST_TAG, objectItem)

    /**
     * multiple items result constructor
     */
    constructor(objectList: List<NotificationConfigInfo>) : this(
        0,
        objectList.size.toLong(),
        TotalHits.Relation.EQUAL_TO,
        objectList
    )

    /**
     * all param constructor
     */
    constructor(
        startIndex: Long,
        totalHits: Long,
        totalHitRelation: TotalHits.Relation,
        objectList: List<NotificationConfigInfo>
    ) : super(startIndex, totalHits, totalHitRelation, CONFIG_LIST_TAG, objectList)

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : super(input, NotificationConfigInfo.reader)

    /**
     * Construct object from XContentParser
     */
    constructor(parser: XContentParser) : super(parser, CONFIG_LIST_TAG)

    /**
     * Construct object from SearchResponse
     */
    constructor(from: Long, response: SearchResponse, searchHitParser: SearchHitParser<NotificationConfigInfo>) : super(
        from,
        response,
        searchHitParser,
        CONFIG_LIST_TAG
    )

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser): NotificationConfigInfo {
        return NotificationConfigInfo.parse(parser)
    }
}
