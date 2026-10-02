/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.action.ActionType;
import org.opensearch.common.CheckedRunnable;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.client.Client;

/**
 * Covers the four behaviors that distinguish this client from the per-plugin copies it replaces:
 * the action runs through the subject, the caller's context is restored exactly once and only after
 * the listener completes, a synchronous failure reaches the listener instead of escaping, and an
 * unassigned subject is a programming error rather than a silent no-op.
 */
public class PluginClientTest {

    private static final String CALLER_TRANSIENT = "caller.marker";
    private static final String SUBJECT_TRANSIENT = "subject.marker";

    private ThreadContext threadContext;
    private Client delegate;
    private AtomicReference<ActionListener<DummyResponse>> capturedListener;

    @BeforeEach
    public void setUp() {
        threadContext = new ThreadContext(Settings.EMPTY);
        capturedListener = new AtomicReference<>();

        ThreadPool threadPool = mock(ThreadPool.class);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        delegate = mock(Client.class);
        when(delegate.settings()).thenReturn(Settings.EMPTY);
        when(delegate.threadPool()).thenReturn(threadPool);

        // Captures the listener without invoking it, so the test controls when the action completes.
        // That is what makes the restore-timing assertion below meaningful.
        doAnswer(invocation -> {
            capturedListener.set(invocation.getArgument(2));
            return null;
        }).when(delegate).execute(any(ActionType.class), any(ActionRequest.class), any(ActionListener.class));
    }

    @Test
    public void runsActionThroughTheAssignedSubject() {
        RecordingSubject subject = new RecordingSubject(threadContext, null);
        PluginClient pluginClient = new PluginClient(delegate);
        pluginClient.setSubject(subject);

        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {}, e -> {}));

        assertEquals(1, subject.runAsInvocations.get());
        assertNotNull(capturedListener.get(), "delegate should have been reached");
    }

    @Test
    public void listenerSeesTheCallerContextEvenOnAThreadThatNeverHadIt() throws Exception {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");

        RecordingSubject subject = new RecordingSubject(threadContext, null);
        PluginClient pluginClient = new PluginClient(delegate);
        pluginClient.setSubject(subject);

        AtomicReference<String> seenByListener = new AtomicReference<>("listener did not run");
        pluginClient
            .execute(
                DUMMY_ACTION,
                new DummyRequest(),
                ActionListener.wrap(r -> seenByListener.set(threadContext.getTransient(CALLER_TRANSIENT)), e -> {})
            );

        // Completing on a separate thread is what makes this discriminate. A transport response
        // arrives on a pooled thread that never carried the caller's context, so the restore wired
        // to the listener is the only thing that can put it back.
        Thread responder = new Thread(() -> capturedListener.get().onResponse(new DummyResponse()));
        responder.start();
        responder.join();

        assertEquals("set-by-caller", seenByListener.get());
    }

    @Test
    public void deliversSynchronousFailureToTheListenerRatherThanThrowing() {
        RuntimeException boom = new RuntimeException("subject refused");
        RecordingSubject subject = new RecordingSubject(threadContext, boom);
        PluginClient pluginClient = new PluginClient(delegate);
        pluginClient.setSubject(subject);

        AtomicReference<Exception> failure = new AtomicReference<>();
        // No assertThrows here on purpose: escaping synchronously would leave an async caller
        // waiting forever, so the contract is that the listener is told.
        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {}, failure::set));

        assertSame(boom, failure.get());
        assertNull(capturedListener.get(), "delegate should not have been reached");
    }

    @Test
    public void rejectsUseBeforeASubjectIsAssigned() {
        PluginClient pluginClient = new PluginClient(delegate);

        IllegalStateException e = assertThrows(
            IllegalStateException.class,
            () -> pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {}, ex -> {}))
        );
        assertEquals("PluginClient is not initialized with a subject.", e.getMessage());
    }

    /**
     * Stands in for the security plugin's subject. Marks the thread context on entry so a test can
     * tell the subject's context apart from the caller's, and can be asked to fail.
     */
    private static class RecordingSubject implements org.opensearch.identity.Subject {

        private final AtomicInteger runAsInvocations = new AtomicInteger();
        private final ThreadContext threadContext;
        private final RuntimeException failure;

        RecordingSubject(ThreadContext threadContext, RuntimeException failure) {
            this.threadContext = threadContext;
            this.failure = failure;
        }

        @Override
        public Principal getPrincipal() {
            return () -> "plugin:test";
        }

        @Override
        public <E extends Exception> void runAs(CheckedRunnable<E> runnable) throws E {
            runAsInvocations.incrementAndGet();
            if (failure != null) {
                throw failure;
            }
            // Mirrors SecurePluginSubject and NoopPluginSubject: both stash, inject, and restore on
            // exit. The restore matters, because it means the calling thread is already back to the
            // caller's context by the time doExecute returns.
            try (ThreadContext.StoredContext ignored = threadContext.stashContext()) {
                threadContext.putTransient(SUBJECT_TRANSIENT, "set-by-subject");
                runnable.run();
            }
        }
    }

    private static final ActionType<DummyResponse> DUMMY_ACTION = new ActionType<>(
        "cluster:admin/plugin_client_test",
        in -> new DummyResponse()
    );

    private static class DummyRequest extends ActionRequest {
        @Override
        public ActionRequestValidationException validate() {
            return null;
        }
    }

    private static class DummyResponse extends ActionResponse {
        @Override
        public void writeTo(StreamOutput out) throws IOException {}
    }
}
