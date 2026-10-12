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

/** Covers the behaviors that distinguish this client from the per-plugin copies it replaces. */
public class PluginClientTests {

    private static final String CALLER_TRANSIENT = "caller.marker";
    private static final String SUBJECT_TRANSIENT = "subject.marker";
    private static final String RESPONDER_TRANSIENT = "responder.marker";

    private static final ActionType<DummyResponse> DUMMY_ACTION = new ActionType<>(
        "cluster:admin/plugin_client_test",
        in -> new DummyResponse()
    );

    private ThreadContext threadContext;
    private PluginClient pluginClient;
    private ActionListener<DummyResponse> capturedListener;

    @BeforeEach
    public void setUp() {
        threadContext = new ThreadContext(Settings.EMPTY);

        ThreadPool threadPool = mock(ThreadPool.class);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        Client delegate = mock(Client.class);
        when(delegate.settings()).thenReturn(Settings.EMPTY);
        when(delegate.threadPool()).thenReturn(threadPool);

        // Captures the listener without invoking it, so each test decides when the action completes.
        doAnswer(invocation -> {
            capturedListener = invocation.getArgument(2);
            return null;
        }).when(delegate).execute(any(ActionType.class), any(ActionRequest.class), any(ActionListener.class));

        pluginClient = new PluginClient(delegate);
    }

    @Test
    public void runsActionThroughTheAssignedSubject() {
        RecordingSubject subject = assignSubject(null);

        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ignoring());

        assertEquals(1, subject.runAsInvocations);
        assertNotNull(capturedListener, "delegate should have been reached");
    }

    @Test
    public void listenerSeesTheCallerContextAndNotTheSubjects() throws Exception {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");
        assignSubject(null);

        AtomicReference<String> callerSeen = new AtomicReference<>("listener did not run");
        AtomicReference<String> subjectSeen = new AtomicReference<>("listener did not run");
        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {
            callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT));
            subjectSeen.set(threadContext.getTransient(SUBJECT_TRANSIENT));
        }, e -> {}));

        // Completing on a separate thread is what makes this discriminate. A transport response
        // arrives on a pooled thread that never carried the caller's context, so the wrapper around
        // the listener is the only thing that can put it back.
        completeOnAnotherThread(null);

        assertEquals("set-by-caller", callerSeen.get());
        assertNull(subjectSeen.get(), "the subject's context must not leak into the listener");
    }

    @Test
    public void restoresTheRespondingThreadsContextAfterTheListenerReturns() throws Exception {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");
        assignSubject(null);

        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ignoring());

        // The responding thread marks itself, completes the action, then checks its own context once
        // the listener has returned. Restoring the caller's context and leaving it there would strand
        // it on a pooled thread for whatever runs next.
        AtomicReference<String> responderAfter = new AtomicReference<>();
        AtomicReference<String> callerLeftBehind = new AtomicReference<>();
        completeOnAnotherThread(() -> {
            responderAfter.set(threadContext.getTransient(RESPONDER_TRANSIENT));
            callerLeftBehind.set(threadContext.getTransient(CALLER_TRANSIENT));
        });

        assertEquals("set-by-responder", responderAfter.get(), "responding thread's own context must come back");
        assertNull(callerLeftBehind.get(), "the caller's context must not be left on the responding thread");
    }

    @Test
    public void deliversSynchronousFailureToTheListenerRatherThanThrowing() {
        RuntimeException boom = new RuntimeException("subject refused");
        assignSubject(boom);

        AtomicReference<Exception> failure = new AtomicReference<>();
        // No assertThrows here on purpose: escaping synchronously would leave an async caller
        // waiting forever, so the contract is that the listener is told.
        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {}, failure::set));

        assertSame(boom, failure.get());
        assertNull(capturedListener, "delegate should not have been reached");
    }

    @Test
    public void rejectsUseBeforeASubjectIsAssigned() {
        IllegalStateException e = assertThrows(
            IllegalStateException.class,
            () -> pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ignoring())
        );
        assertEquals("PluginClient is not initialized with a subject.", e.getMessage());
    }

    private RecordingSubject assignSubject(RuntimeException failure) {
        RecordingSubject subject = new RecordingSubject(threadContext, failure);
        pluginClient.setSubject(subject);
        return subject;
    }

    /** Completes the captured action on a thread that never carried the caller's context. */
    private void completeOnAnotherThread(Runnable afterListenerReturns) throws InterruptedException {
        Thread responder = new Thread(() -> {
            threadContext.putTransient(RESPONDER_TRANSIENT, "set-by-responder");
            capturedListener.onResponse(new DummyResponse());
            if (afterListenerReturns != null) {
                afterListenerReturns.run();
            }
        });
        responder.start();
        responder.join();
    }

    private static ActionListener<DummyResponse> ignoring() {
        return ActionListener.wrap(r -> {}, e -> {});
    }

    /**
     * Stands in for the security plugin's subject. Stashes and restores around its body the way both
     * real implementations do, and marks the context so a test can tell the subject's apart from the
     * caller's.
     */
    private static class RecordingSubject implements org.opensearch.identity.Subject {

        private final ThreadContext threadContext;
        private final RuntimeException failure;
        private int runAsInvocations;

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
            runAsInvocations++;
            if (failure != null) {
                throw failure;
            }
            try (ThreadContext.StoredContext ignored = threadContext.stashContext()) {
                threadContext.putTransient(SUBJECT_TRANSIENT, "set-by-subject");
                runnable.run();
            }
        }
    }

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
