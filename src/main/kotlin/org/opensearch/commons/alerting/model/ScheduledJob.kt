package org.opensearch.commons.alerting.model

import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
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

        // Field names that are registered ScheduledJob subtypes and therefore dispatch to the
        // corresponding parser via [XContentParser.namedObject]. Anything else at the top level
        // of a stored doc is treated as ancillary data (for example `all_shared_principals`
        // injected by the security plugin's resource-sharing framework for DLS) and skipped.
        private val SCHEDULED_JOB_WRAPPER_FIELDS = setOf("monitor", "workflow")

        /**
         * This function parses the job, delegating to the specific subtype parser registered in
         * the [XContentParser.getXContentRegistry] at runtime. Each concrete job subclass is
         * expected to register a parser in this registry. The Job's JSON representation is
         * expected to be of the form:
         *     { "<job_type>" : { <job fields> } }
         * Any additional top-level fields (for example `all_shared_principals` injected by the
         * security plugin's resource-sharing framework) are tolerated and skipped.
         *
         * If the job comes from an OpenSearch index its [id] and [version] can also be supplied.
         */

        @Throws(IOException::class)
        fun parse(xcp: XContentParser, id: String = NO_ID, version: Long = NO_VERSION): ScheduledJob {
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
            var job: ScheduledJob? = null
            var token = xcp.nextToken()
            while (token != null && token != XContentParser.Token.END_OBJECT) {
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, token, xcp)
                val fieldName = xcp.currentName()
                xcp.nextToken() // advance to value
                if (fieldName in SCHEDULED_JOB_WRAPPER_FIELDS && job == null) {
                    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
                    job = xcp.namedObject(ScheduledJob::class.java, fieldName, null)
                } else {
                    xcp.skipChildren()
                }
                token = xcp.nextToken()
            }
            requireNotNull(job) { "No recognized ScheduledJob subtype wrapper found in document" }
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, token, xcp)
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
            // Skip any trailing top-level fields that live after the wrapper (for example the
            // security plugin's `all_shared_principals` DLS field) — see the no-arg overload above.
            var next = xcp.nextToken()
            while (next != null && next != XContentParser.Token.END_OBJECT) {
                if (next == XContentParser.Token.FIELD_NAME) {
                    xcp.nextToken()
                    xcp.skipChildren()
                }
                next = xcp.nextToken()
            }
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, next, xcp)
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
