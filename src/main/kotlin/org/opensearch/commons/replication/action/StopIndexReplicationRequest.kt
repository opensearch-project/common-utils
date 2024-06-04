package org.opensearch.commons.replication.action

import org.opensearch.action.ActionRequestValidationException
import org.apache.logging.log4j.LogManager
import org.opensearch.action.IndicesRequest
import org.opensearch.action.support.IndicesOptions
import org.opensearch.action.support.master.AcknowledgedRequest
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.*
class StopIndexReplicationRequest : AcknowledgedRequest<StopIndexReplicationRequest>, IndicesRequest.Replaceable, ToXContentObject  {
    lateinit var indexName: String
    constructor(indexName: String) {
        this.indexName = indexName
    }

    private constructor() {
    }

    constructor(inp: StreamInput): super(inp) {
        indexName = inp.readString()
    }
    companion object {
        private val PARSER = ObjectParser<StopIndexReplicationRequest, Void>("StopReplicationRequestParser") {
            StopIndexReplicationRequest()
        }

        fun fromXContent(parser: XContentParser, followerIndex: String): StopIndexReplicationRequest {
            val stopIndexReplicationRequest = PARSER.parse(parser, null)
            stopIndexReplicationRequest.indexName = followerIndex
            return stopIndexReplicationRequest
        }
        private val log = LogManager.getLogger(StopIndexReplicationRequest::class.java)
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    override fun indices(vararg indices: String?): IndicesRequest {
        return this
    }
    override fun indices(): Array<String> {
        return arrayOf(indexName)
    }

    override fun indicesOptions(): IndicesOptions {
        return IndicesOptions.strictSingleIndexNoExpandForbidClosed()
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        builder.field("indexName", indexName)
        builder.endObject()
        return builder
    }

    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
        out.writeString(indexName)
    }
}