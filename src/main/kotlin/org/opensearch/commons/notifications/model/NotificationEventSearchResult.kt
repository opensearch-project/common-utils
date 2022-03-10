/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.model

import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.NotificationConstants.EVENT_LIST_TAG

/**
 * Notification Event search results
 */
class NotificationEventSearchResult : SearchResults<NotificationEventInfo> {

    /**
     * single item result constructor
     */
    constructor(objectItem: NotificationEventInfo) : super(EVENT_LIST_TAG, objectItem)

    /**
     * multiple items result constructor
     */
    constructor(objectList: List<NotificationEventInfo>) : this(
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
        objectList: List<NotificationEventInfo>
    ) : super(startIndex, totalHits, totalHitRelation, EVENT_LIST_TAG, objectList)

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : super(input, NotificationEventInfo.reader)

    /**
     * Construct object from XContentParser
     */
    constructor(parser: XContentParser) : super(parser, EVENT_LIST_TAG)

    /**
     * Construct object from SearchResponse
     */
    constructor(from: Long, response: SearchResponse, searchHitParser: SearchHitParser<NotificationEventInfo>) : super(
        from,
        response,
        searchHitParser,
        EVENT_LIST_TAG
    )

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser): NotificationEventInfo {
        return NotificationEventInfo.parse(parser)
    }
}
