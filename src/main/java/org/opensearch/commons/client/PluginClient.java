/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionType;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.identity.Subject;
import org.opensearch.transport.client.Client;
import org.opensearch.transport.client.FilterClient;

/**
 * Executes transport actions as the consuming plugin's assigned system subject rather than as the
 * authenticated user, which is how a plugin reaches the system indices it owns. Replaces wrapping
 * client calls in {@code ThreadContext.stashContext()}, which grants unrestricted access rather
 * than access scoped to the plugin's own registered system indices.
 * <p>
 * A plugin constructs one of these around the client it receives in {@code createComponents}, then
 * implements {@code IdentityAwarePlugin} and passes the subject along in {@code assignSubject}:
 *
 * <pre>
 * public void assignSubject(PluginSubject pluginSubject) {
 *     this.pluginClient.setSubject(pluginSubject);
 * }
 * </pre>
 *
 * The subject is assigned whether or not the security plugin is installed, so there is no
 * conditional path for the unsecured case.
 */
public class PluginClient extends FilterClient {

    private static final Logger log = LogManager.getLogger(PluginClient.class);

    // Assigned from IdentityAwarePlugin.assignSubject, which runs on a different thread than the
    // transport actions that read it.
    private volatile Subject subject;

    public PluginClient(Client delegate) {
        super(delegate);
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    protected <Request extends ActionRequest, Response extends ActionResponse> void doExecute(
        ActionType<Response> action,
        Request request,
        ActionListener<Response> listener
    ) {
        Subject currentSubject = this.subject;
        if (currentSubject == null) {
            throw new IllegalStateException("PluginClient is not initialized with a subject.");
        }

        // Saves the caller's context so the listener can be given it back. runAs performs the switch
        // itself and restores on exit, so this is about the listener, which runs later and on a
        // thread that never carried the caller's context. newStoredContext(false) over stashContext()
        // and a local over try-with-resources are both for clarity; neither changes behavior here.
        ThreadContext.StoredContext storedContext = threadPool().getThreadContext().newStoredContext(false);

        try {
            currentSubject.runAs(() -> {
                log.debug("Running transport action as subject: {}", currentSubject.getPrincipal().getName());
                super.doExecute(action, request, ActionListener.runBefore(listener, storedContext::restore));
            });
        } catch (Exception e) {
            // Reported through the listener rather than thrown, so an async caller is not left waiting.
            storedContext.close();
            listener.onFailure(e);
        }
    }
}
