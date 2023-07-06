/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.listener;

import org.opensearch.client.node.NodeClient;
import org.opensearch.commons.alerting.model.Finding;

public interface AlertingEventListener {

    default void onFindingCreated(NodeClient client, String monitorId, Finding finding) {}
}
