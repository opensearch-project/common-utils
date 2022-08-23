/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.model2.action;

import org.opensearch.action.ActionType;

public class IndexMonitorAction extends ActionType<IndexMonitorResponse> {

    public static final String ALERTING_NAME = "cluster:admin/opendistro/security_analytics/monitor/index";
    public static final IndexMonitorAction ALERTING_INSTANCE = new IndexMonitorAction(ALERTING_NAME);

    private IndexMonitorAction(final String name) {
        super(name, IndexMonitorResponse::new);
    }
}
