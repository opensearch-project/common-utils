package org.opensearch.commons.alerting.action

import org.opensearch.Version
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.util.IndexUtils.Companion.INCLUDE_BACKEND_ROLES_PARAM
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException
import java.util.Collections

class GetAlertsResponse : BaseResponse {
    val alerts: List<Alert>

    // totalAlerts is not the same as the size of alerts because there can be 30 alerts from the request, but
    // the request only asked for 5 alerts, so totalAlerts will be 30, but alerts will only contain 5 alerts
    val totalAlerts: Int?

    /**
     * Alert id to the monitor backend roles that the requesting user is entitled to see for that alert, resolved
     * by the transport action. Alerts missing from the map have no backend roles written to the response.
     */
    val visibleBackendRoles: Map<String, List<String>>?

    constructor(
        alerts: List<Alert>,
        totalAlerts: Int?
    ) : this(alerts, totalAlerts, null)

    constructor(
        alerts: List<Alert>,
        totalAlerts: Int?,
        visibleBackendRoles: Map<String, List<String>>?
    ) : super() {
        this.alerts = alerts
        this.totalAlerts = totalAlerts
        this.visibleBackendRoles = visibleBackendRoles
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        alerts = Collections.unmodifiableList(sin.readList(::Alert)),
        totalAlerts = sin.readOptionalInt(),
        visibleBackendRoles = if (sin.version.onOrAfter(Version.V_3_10_0) && sin.readBoolean()) {
            sin.readMap({ it.readString() }, { it.readStringList() })
        } else {
            null
        }
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeCollection(alerts)
        out.writeOptionalInt(totalAlerts)
        if (out.version.onOrAfter(Version.V_3_10_0)) {
            val visibleBackendRoles = this.visibleBackendRoles
            if (visibleBackendRoles == null) {
                out.writeBoolean(false)
            } else {
                out.writeBoolean(true)
                out.writeMap(
                    visibleBackendRoles,
                    { o, key -> o.writeString(key) },
                    { o, roles -> o.writeStringCollection(roles) }
                )
            }
        }
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        val includeBackendRoles = params.paramAsBoolean(INCLUDE_BACKEND_ROLES_PARAM, false)
        builder.startObject()
            .startArray("alerts")
        alerts.forEach { alert ->
            // Nothing to show means nothing is written: no empty monitor_user block for an alert whose
            // monitor carries no roles, or whose roles the requester shares none of.
            val visibleBackendRoles = if (includeBackendRoles) {
                this.visibleBackendRoles?.get(alert.id)?.takeIf { it.isNotEmpty() }
            } else {
                null
            }
            if (visibleBackendRoles != null) {
                alert.toXContentWithBackendRoles(builder, visibleBackendRoles)
            } else {
                alert.toXContent(builder, params)
            }
        }
        builder.endArray()
            .field("totalAlerts", totalAlerts)

        return builder.endObject()
    }
}
