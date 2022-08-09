package org.opensearch.commons.alerting.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.opensearch.commons.alerting.action.ExecuteMonitorAction
import org.opensearch.commons.alerting.action.ExecuteMonitorRequest
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.RestHandler
import org.opensearch.rest.RestRequest
import org.opensearch.common.xcontent.XContentParser.Token.START_OBJECT
import org.opensearch.commons.utils.logger
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.action.RestToXContentListener
import java.time.Instant

class RestExecuteMonitorAction : BaseRestHandler() {

    companion object {
        @JvmField val OPEN_SEARCH_DASHBOARDS_USER_AGENT = "OpenSearch-Dashboards"
        @JvmField val UI_METADATA_EXCLUDE = arrayOf("monitor.${Monitor.UI_METADATA_FIELD}")
        @JvmField val MONITOR_BASE_URI = "/_plugins/_alerting/monitors"
        @JvmField val DESTINATION_BASE_URI = "/_plugins/_alerting/destinations"
        @JvmField val LEGACY_OPENDISTRO_MONITOR_BASE_URI = "/_opendistro/_alerting/monitors"
        @JvmField val LEGACY_OPENDISTRO_DESTINATION_BASE_URI = "/_opendistro/_alerting/destinations"
        @JvmField val EMAIL_ACCOUNT_BASE_URI = "$DESTINATION_BASE_URI/email_accounts"
        @JvmField val EMAIL_GROUP_BASE_URI = "$DESTINATION_BASE_URI/email_groups"
        @JvmField val LEGACY_OPENDISTRO_EMAIL_ACCOUNT_BASE_URI = "$LEGACY_OPENDISTRO_DESTINATION_BASE_URI/email_accounts"
        @JvmField val LEGACY_OPENDISTRO_EMAIL_GROUP_BASE_URI = "$LEGACY_OPENDISTRO_DESTINATION_BASE_URI/email_groups"
        @JvmField val FINDING_BASE_URI = "/_plugins/_alerting/findings"
        @JvmField val ALERTING_JOB_TYPES = listOf("monitor")
    }

    override fun getName(): String = "execute_monitor_action"

    override fun routes(): List<RestHandler.Route> {
        return listOf()
    }

    override fun replacedRoutes(): MutableList<RestHandler.ReplacedRoute> {
        return mutableListOf(
            RestHandler.ReplacedRoute(
                POST,
                "${MONITOR_BASE_URI}/{monitorID}/_execute",
                POST,
                "${LEGACY_OPENDISTRO_MONITOR_BASE_URI}/{monitorID}/_execute"
            ),
            RestHandler.ReplacedRoute(
                POST,
                "${MONITOR_BASE_URI}/_execute",
                POST,
                "${LEGACY_OPENDISTRO_MONITOR_BASE_URI}/_execute"
            )
        )
    }

    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return RestChannelConsumer { channel ->
            val dryrun = request.paramAsBoolean("dryrun", false)
            val requestEnd = request.paramAsTime("period_end", TimeValue(Instant.now().toEpochMilli()))

            if (request.hasParam("monitorID")) {
                val monitorId = request.param("monitorID")
                val execMonitorRequest = ExecuteMonitorRequest(dryrun, requestEnd, monitorId, null)
                client.execute(ExecuteMonitorAction.INSTANCE, execMonitorRequest, RestToXContentListener(channel))
            } else {
                val xcp = request.contentParser()
                ensureExpectedToken(START_OBJECT, xcp.nextToken(), xcp)
                val monitor = Monitor.parse(xcp, Monitor.NO_ID, Monitor.NO_VERSION)
                val execMonitorRequest = ExecuteMonitorRequest(dryrun, requestEnd, null, monitor)
                client.execute(ExecuteMonitorAction.INSTANCE, execMonitorRequest, RestToXContentListener(channel))
            }
        }
    }

    override fun responseParams(): Set<String> {
        return setOf("dryrun", "period_end", "monitorID")
    }
}