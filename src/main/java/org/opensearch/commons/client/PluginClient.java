/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionType;
import org.opensearch.action.support.ContextPreservingActionListener;
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

    /**
     * Wraps a client so its actions run as the plugin subject. The subject arrives later, through
     * {@link #setSubject(Subject)}, so actions executed before then fail fast.
     *
     * @param delegate the client whose actions this wraps
     */
    public PluginClient(Client delegate) {
        super(delegate);
    }

    /**
     * Wraps a client with its subject already known, for callers that have one before they build the
     * client. Core assigns the subject after {@code createComponents}, so most plugins cannot use this.
     *
     * @param delegate the client whose actions this wraps
     * @param subject the plugin's system subject
     */
    public PluginClient(Client delegate, Subject subject) {
        super(delegate);
        this.subject = subject;
    }

    /**
     * Assigns the subject every subsequent action runs as, normally the one handed to
     * {@code IdentityAwarePlugin.assignSubject}.
     *
     * @param subject the plugin's system subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * Runs the action as the assigned subject.
     *
     * @param listener notified with the caller's thread context in place, not the subject's
     * @throws IllegalStateException if no subject has been assigned yet
     */
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

        // Snapshot taken before runAs, which clears the context. Core's wrapper restores the caller's
        // context around the callback and puts the responding thread's own back afterwards, so the
        // caller's context is not left behind on a pooled thread.
        ActionListener<Response> callerContext = ContextPreservingActionListener
            .wrapPreservingContext(listener, threadPool().getThreadContext());

        try {
            currentSubject.runAs(() -> {
                log.debug("Running transport action as subject: {}", currentSubject.getPrincipal().getName());
                super.doExecute(action, request, callerContext);
            });
        } catch (Exception e) {
            // Reported through the listener so an async caller is not left waiting.
            listener.onFailure(e);
        }
    }
}
