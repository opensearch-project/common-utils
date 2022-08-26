/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.model2.action;

import org.opensearch.action.ActionType;

public class IndexMonitorAction extends ActionType<IndexMonitorResponse> {

    public static final String ACTION_TEMPLATE = "cluster:admin/opendistro/${plugin}/monitor/${method}";
    public static final IndexMonitorAction ALERTING_BRIDGE_INSTANCE = new IndexMonitorAction(ACTION_TEMPLATE.replace("${plugin}","alerting").replace("${method}","write2"));
    public static final IndexMonitorAction SAP_INSTANCE = new IndexMonitorAction(ACTION_TEMPLATE.replace("${plugin}","security").replace("${method}","write"));

    private IndexMonitorAction(final String name) {
        super(name, IndexMonitorResponse::new);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + this.name() + "]";
    }
}
