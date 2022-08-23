/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.model2.action;

import org.opensearch.action.ActionType;

public class IndexMonitorAction extends ActionType<IndexMonitorResponse> {

    public static final String SAP_ALERTING_BRIDGE_NAME = "cluster:admin/opendistro/alerting/monitor/write2";
    public static final IndexMonitorAction SAP_ALERTING_BRIDGE_INSTANCE = new IndexMonitorAction(SAP_ALERTING_BRIDGE_NAME);

    private IndexMonitorAction(final String name) {
        super(name, IndexMonitorResponse::new);
    }
}
