package org.opensearch.commons.alerting.resthandler

import org.opensearch.action.support.WriteRequest
import org.opensearch.alerting.action.IndexMonitorRequest
import org.opensearch.client.node.NodeClient
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.opensearch.commons.alerting.action.*
import org.opensearch.commons.alerting.model.BucketLevelTrigger
import org.opensearch.commons.alerting.model.DocumentLevelTrigger
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.QueryLevelTrigger
import org.opensearch.commons.alerting.resthandler.RestExecuteMonitorAction.Companion.LEGACY_OPENDISTRO_MONITOR_BASE_URI
import org.opensearch.commons.alerting.resthandler.RestExecuteMonitorAction.Companion.MONITOR_BASE_URI
import org.opensearch.index.seqno.SequenceNumbers
import org.opensearch.rest.*
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestRequest.Method.PUT
import org.opensearch.rest.action.RestResponseListener
import java.io.IOException
import java.time.Instant

/**
 * Rest handlers to create and update monitors.
 */
class RestIndexMonitorAction : BaseRestHandler() {

    override fun getName(): String {
        return "index_monitor_action"
    }

    override fun routes(): List<RestHandler.Route> {
        return listOf()
    }

    override fun replacedRoutes(): MutableList<RestHandler.ReplacedRoute> {
        return mutableListOf(
            RestHandler.ReplacedRoute(
                POST,
                MONITOR_BASE_URI,
                POST,
                LEGACY_OPENDISTRO_MONITOR_BASE_URI
            ),
            RestHandler.ReplacedRoute(
                PUT,
                "${MONITOR_BASE_URI}/{monitorID}",
                PUT,
                "${LEGACY_OPENDISTRO_MONITOR_BASE_URI}/{monitorID}"
            )
        )
    }

    @Throws(IOException::class)
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val id = request.param("monitorID", Monitor.NO_ID)
        if (request.method() == PUT && Monitor.NO_ID == id) {
            throw IllegalArgumentException("Missing monitor ID")
        }

        // Validate request by parsing JSON to Monitor
        val xcp = request.contentParser()
        ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp)
        val monitor = Monitor.parse(xcp, id).copy(lastUpdateTime = Instant.now())
        val monitorType = monitor.monitorType
        val triggers = monitor.triggers
        when (monitorType) {
            Monitor.MonitorType.QUERY_LEVEL_MONITOR -> {
                triggers.forEach {
                    if (it !is QueryLevelTrigger) {
                        throw IllegalArgumentException("Illegal trigger type, ${it.javaClass.name}, for query level monitor")
                    }
                }
            }
            Monitor.MonitorType.BUCKET_LEVEL_MONITOR -> {
                triggers.forEach {
                    if (it !is BucketLevelTrigger) {
                        throw IllegalArgumentException("Illegal trigger type, ${it.javaClass.name}, for bucket level monitor")
                    }
                }
            }
            Monitor.MonitorType.DOC_LEVEL_MONITOR -> {
                triggers.forEach {
                    if (it !is DocumentLevelTrigger) {
                        throw IllegalArgumentException("Illegal trigger type, ${it.javaClass.name}, for document level monitor")
                    }
                }
            }
        }
        val seqNo = request.paramAsLong(IF_SEQ_NO, SequenceNumbers.UNASSIGNED_SEQ_NO)
        val primaryTerm = request.paramAsLong(IF_PRIMARY_TERM, SequenceNumbers.UNASSIGNED_PRIMARY_TERM)
        val refreshPolicy = if (request.hasParam(REFRESH)) {
            WriteRequest.RefreshPolicy.parse(request.param(REFRESH))
        } else {
            WriteRequest.RefreshPolicy.IMMEDIATE
        }
        val indexMonitorRequest = IndexMonitorRequest(id, seqNo, primaryTerm, refreshPolicy, request.method(), monitor)

        return RestChannelConsumer { channel ->
            client.execute(
                IndexMonitorAction.INSTANCE,
                indexMonitorRequest,
                indexMonitorResponse(channel, request.method())
            )
        }
    }

    private fun indexMonitorResponse(channel: RestChannel, restMethod: RestRequest.Method):
            RestResponseListener<IndexMonitorResponse> {
        return object : RestResponseListener<IndexMonitorResponse>(channel) {
            @Throws(Exception::class)
            override fun buildResponse(response: IndexMonitorResponse): RestResponse {
                var returnStatus = RestStatus.CREATED
                if (restMethod == RestRequest.Method.PUT)
                    returnStatus = RestStatus.OK

                val restResponse =
                    BytesRestResponse(returnStatus, response.toXContent(channel.newBuilder(), ToXContent.EMPTY_PARAMS))
                if (returnStatus == RestStatus.CREATED) {
                    val location = "${MONITOR_BASE_URI}/${response.id}"
                    restResponse.addHeader("Location", location)
                }
                return restResponse
            }
        }
    }
}