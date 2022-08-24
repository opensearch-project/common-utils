package org.opensearch.commons.alerting.model

import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import java.io.IOException
import java.time.Instant

interface ScheduledJob : BaseModel {

    fun toXContentWithType(builder: XContentBuilder): XContentBuilder = toXContent(builder, XCONTENT_WITH_TYPE)

    companion object {
        /** The name of the ElasticSearch index in which we store jobs */
        const val SCHEDULED_JOBS_INDEX = ".opendistro-alerting-config"
        const val DOC_LEVEL_QUERIES_INDEX = ".opensearch-alerting-queries"

        const val NO_ID = ""

        const val NO_VERSION = 1L

        private val XCONTENT_WITH_TYPE = ToXContent.MapParams(mapOf("with_type" to "true"))

        /**
         * This function parses the job, delegating to the specific subtype parser registered in the [XContentParser.getXContentRegistry]
         * at runtime.  Each concrete job subclass is expected to register a parser in this registry.
         * The Job's json representation is expected to be of the form:
         *     { "<job_type>" : { <job fields> } }
         *
         * If the job comes from an OpenSearch index it's [id] and [version] can also be supplied.
         */
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): ScheduledJob {
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, xcp.nextToken(), xcp)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            val job = xcp.namedObject(ScheduledJob::class.java, xcp.currentName(), null)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, xcp.nextToken(), xcp)
            return job.fromDocument(id, version)
        }

        /**
         * This function parses the job, but expects the type to be passed in. This is for the specific
         * use case in sweeper where we first want to check if the job is allowed to be swept before
         * trying to fully parse it. If you need to parse a job, you most likely want to use
         * the above parse function.
         */
        @Throws(IOException::class)
        fun parse(xcp: XContentParser, type: String, id: String = NO_ID, version: Long = NO_VERSION): ScheduledJob {
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            val job = xcp.namedObject(ScheduledJob::class.java, type, null)
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, xcp.nextToken(), xcp)
            return job.fromDocument(id, version)
        }
    }

    /** The id of the job in the [SCHEDULED_JOBS_INDEX] or [NO_ID] if not persisted */
    val id: String

    /** The version of the job in the [SCHEDULED_JOBS_INDEX] or [NO_VERSION] if not persisted  */
    val version: Long

    /** The name of the job */
    val name: String

    /** The type of the job */
    val type: String

    /** Controls whether the job will be scheduled or not  */
    val enabled: Boolean

    /** The schedule for running the job  */
    val schedule: Schedule

    /** The last time the job was updated */
    val lastUpdateTime: Instant

    /** The time the job was enabled */
    val enabledTime: Instant?

    /** Copy constructor for persisted jobs */
    fun fromDocument(id: String, version: Long): ScheduledJob
}
