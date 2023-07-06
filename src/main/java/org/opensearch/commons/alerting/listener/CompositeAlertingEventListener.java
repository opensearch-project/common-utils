/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.opensearch.action.ActionListener;
import org.opensearch.action.ActionType;
import org.opensearch.client.node.NodeClient;
import org.opensearch.commons.alerting.AlertingPluginInterface;
import org.opensearch.commons.alerting.action.PublishFindingsRequest;
import org.opensearch.commons.alerting.action.SubscribeFindingsResponse;
import org.opensearch.commons.alerting.model.Finding;

final class CompositeAlertingEventListener implements AlertingEventListener {

    private final List<String> listeners;
    private static final Logger log = LogManager.getLogger(CompositeAlertingEventListener.class);

    CompositeAlertingEventListener(Collection<String> listeners) {
        this.listeners = Collections.unmodifiableList(new ArrayList<>(listeners));
    }

    @Override
    public void onFindingCreated(NodeClient client, String monitorId, Finding finding) {
        log.info("hit here onFindingCreated");
        for (String listener : listeners) {
            try {
                log.info("hit here onFindingCreated-" + listener);
                var SUBSCRIBE_FINDINGS_ACTION_TYPE = new ActionType<>(listener, SubscribeFindingsResponse::new);
                var request = new PublishFindingsRequest(monitorId, finding);
                AlertingPluginInterface.INSTANCE.publishFinding(client, SUBSCRIBE_FINDINGS_ACTION_TYPE, request, new ActionListener<>() {
                    @Override
                    public void onResponse(SubscribeFindingsResponse subscribeFindingsResponse) {
                        log.info(subscribeFindingsResponse.getStatus().getStatus());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.error(e);
                    }
                });
            } catch (Exception e) {
                log
                    .warn(
                        () -> new ParameterizedMessage(
                            "failed to run finding callback for finding with id: {} & monitor id: {}",
                            finding.getId(),
                            monitorId
                        )
                    );
            }
        }
    }
}
