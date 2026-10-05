/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.DocRequest
import org.opensearch.commons.alerting.model.ScheduledJob
import org.opensearch.commons.alerting.model.Table
import org.opensearch.commons.alerting.util.AlertingConstants
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.index.query.BoolQueryBuilder
import java.io.IOException

class GetFindingsRequest : ActionRequest, DocRequest {
    val findingId: String?
    val table: Table
    val monitorId: String?
    val monitorIds: List<String>?
    val findingIndex: String?
    val boolQueryBuilder: BoolQueryBuilder?
    constructor(
        findingId: String?,
        table: Table,
        monitorId: String? = null,
        findingIndexName: String? = null,
        monitorIds: List<String>? = null,
        boolQueryBuilder: BoolQueryBuilder? = null
    ) : super() {
        this.findingId = findingId
        this.table = table
        this.monitorId = monitorId
        this.findingIndex = findingIndexName
        this.monitorIds = monitorIds
        this.boolQueryBuilder = boolQueryBuilder
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        findingId = sin.readOptionalString(),
        table = Table.readFrom(sin),
        monitorId = sin.readOptionalString(),
        findingIndexName = sin.readOptionalString(),
        monitorIds = sin.readOptionalStringList(),
        boolQueryBuilder = BoolQueryBuilder(sin)
    )

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeOptionalString(findingId)
        table.writeTo(out)
        out.writeOptionalString(monitorId)
        out.writeOptionalString(findingIndex)
        out.writeOptionalStringCollection(monitorIds)
        boolQueryBuilder?.writeTo(out)
    }

    override fun index(): String? {
        return ScheduledJob.SCHEDULED_JOBS_INDEX
    }

    override fun id(): String? {
        // Access is gated on the underlying monitor when a single monitorId is provided; otherwise fall back to
        // search-level DLS filtering.
        return monitorId ?: monitorIds?.singleOrNull()
    }

    override fun type(): String {
        return AlertingConstants.MONITOR_RESOURCE_TYPE
    }
}
