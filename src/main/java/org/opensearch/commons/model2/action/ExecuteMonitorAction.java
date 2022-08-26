package org.opensearch.commons.model2.action;

import org.opensearch.action.ActionType;

public class ExecuteMonitorAction extends ActionType<ExecuteMonitorResponse> {

    public static final String ACTION_TEMPLATE = "cluster:admin/opendistro/${plugin}/monitor/${method}";
    public static final ExecuteMonitorAction ALERTING_BRIDGE_INSTANCE = new ExecuteMonitorAction(ACTION_TEMPLATE.replace("${plugin}","alerting").replace("${method}","execute2"));
    public static final ExecuteMonitorAction SAP_INSTANCE = new ExecuteMonitorAction(ACTION_TEMPLATE.replace("${plugin}","security").replace("${method}","execute"));

    private ExecuteMonitorAction(final String name) {
        super(name, ExecuteMonitorResponse::new);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + this.name() + "]";
    }
}