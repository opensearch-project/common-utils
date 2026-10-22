/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.client;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionType;
import org.opensearch.action.support.ContextPreservingActionListener;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.identity.PluginSubject;
import org.opensearch.transport.client.Client;
import org.opensearch.transport.client.FilterClient;

/**
 * Runs transport actions as the consuming plugin's system subject rather than as the authenticated
 * user, so the plugin can reach the system indices it registered. Use it instead of
 * {@code ThreadContext.stashContext()}: a stashed context carries no user, and the security plugin
 * lets a local request with no user through without evaluating privileges, whereas the plugin
 * subject is authorized only for the plugin's own system indices and the permissions it declares.
 * <p>
 * Core assigns the subject through {@code IdentityAwarePlugin.assignSubject} after
 * {@code createComponents}, with or without the security plugin installed. Build this client in
 * {@code createComponents} and call {@link #setSubject(PluginSubject)} from {@code assignSubject}.
 * Apart from the identity it runs as, it behaves like the client it wraps, including which failures
 * are thrown and which reach the listener.
 * <p>
 * Each plugin declares its own subclass. Core binds every component returned from
 * {@code createComponents} by its runtime class, and plugins sharing a classloader through
 * {@code extendedPlugins} share this class, so a shared type would let one plugin inject another's
 * client and run as its subject.
 */
public abstract class PluginClient extends FilterClient {

    private static final Logger log = LogManager.getLogger(PluginClient.class);

    // Set once, from assignSubject, on a different thread from the actions that read it.
    private final AtomicReference<PluginSubject> subject = new AtomicReference<>();

    /**
     * Wraps a client so its actions run as the plugin subject, which is assigned later.
     *
     * @param delegate the client whose actions this wraps
     */
    protected PluginClient(Client delegate) {
        super(delegate);
    }

    /**
     * Wraps a client with its subject already known.
     *
     * @param delegate the client whose actions this wraps
     * @param subject the plugin's system subject
     * @throws NullPointerException if the subject is null
     */
    protected PluginClient(Client delegate, PluginSubject subject) {
        super(delegate);
        setSubject(subject);
    }

    /**
     * Assigns the subject every action runs as. It can be assigned once; assigning the same subject
     * again is a no-op.
     *
     * @param subject the plugin's system subject, normally the one passed to {@code assignSubject}
     * @throws NullPointerException if the subject is null
     * @throws IllegalStateException if a different subject is already assigned
     */
    public final void setSubject(PluginSubject subject) {
        Objects.requireNonNull(subject, "subject");
        PluginSubject existing = this.subject.compareAndExchange(null, subject);
        if (existing != null && existing != subject) {
            throw new IllegalStateException("PluginClient already has a subject assigned.");
        }
    }

    /**
     * Not supported, because a plugin subject has no meaning on another cluster.
     *
     * @param clusterAlias the remote cluster that was requested
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public final Client getRemoteClusterClient(String clusterAlias) {
        throw new UnsupportedOperationException("PluginClient cannot run actions on a remote cluster.");
    }

    /**
     * Runs the action as the assigned subject.
     *
     * @param action the action to run as the subject
     * @param request the request to run it with
     * @param listener notified with the caller's thread context in place rather than the subject's
     * @throws IllegalStateException if no subject has been assigned yet, which is a wiring error
     */
    @Override
    protected final <Request extends ActionRequest, Response extends ActionResponse> void doExecute(
        ActionType<Response> action,
        Request request,
        ActionListener<Response> listener
    ) {
        PluginSubject currentSubject = subject.get();
        if (currentSubject == null) {
            throw new IllegalStateException("PluginClient is not initialized with a subject.");
        }

        // Captured before runAs stashes the context; core restores it around the callback, then puts
        // the responding thread's own context back.
        ActionListener<Response> callerContext = ContextPreservingActionListener
            .wrapPreservingContext(listener, threadPool().getThreadContext());
        currentSubject.runAs(() -> {
            log.debug("Running transport action as subject: {}", currentSubject.getPrincipal().getName());
            super.doExecute(action, request, callerContext);
        });
    }
}
