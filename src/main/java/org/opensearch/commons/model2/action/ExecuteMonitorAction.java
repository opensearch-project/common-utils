package org.opensearch.commons.model2.action;

import org.opensearch.action.ActionType;

public class ExecuteMonitorAction extends ActionType<ExecuteMonitorResponse> {

    public static final String ALERTING_NAME = "cluster:admin/opendistro/security_analytics/monitor/execute";
    public static final ExecuteMonitorAction ALERTING_INSTANCE = new ExecuteMonitorAction(ALERTING_NAME);

    private ExecuteMonitorAction(final String name) {
        super(name, ExecuteMonitorResponse::new);
    }
}