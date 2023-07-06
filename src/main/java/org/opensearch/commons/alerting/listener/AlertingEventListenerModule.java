/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AlertingEventListenerModule {

    private static final Logger log = LogManager.getLogger(AlertingEventListenerModule.class);

    private final Set<String> alertingEventListeners = new HashSet<>();
    private final AtomicBoolean frozen = new AtomicBoolean(false);
    private static AlertingEventListenerModule eventListenerModule;

    public static AlertingEventListenerModule instance() {
        if (eventListenerModule == null) {
            eventListenerModule = new AlertingEventListenerModule();
        }
        return eventListenerModule;
    }

    public void addAlertingEventListener(String listener) {
        log.info("hit here - addAlertingEventListener");
        ensureNotFrozen();
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (alertingEventListeners.contains(listener)) {
            throw new IllegalArgumentException("listener already added");
        }
        log.info("hit here - addAlertingEventListener- " + alertingEventListeners.size());
        alertingEventListeners.add(listener);
        log.info("hit here - addAlertingEventListener- " + alertingEventListeners.size());
    }

    public AlertingEventListener freeze() {
        log.info("hit here freeze-" + alertingEventListeners.size());
        if (this.frozen.compareAndSet(false, true)) {
            return new CompositeAlertingEventListener(alertingEventListeners);
        } else {
            throw new IllegalStateException("already frozen");
        }
    }

    private void ensureNotFrozen() {
        if (this.frozen.get()) {
            throw new IllegalStateException("Can't modify AlertingEventListenerModule once Alerting service is created");
        }
    }
}
