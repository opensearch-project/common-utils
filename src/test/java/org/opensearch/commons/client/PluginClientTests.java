/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
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
import org.opensearch.identity.PluginSubject;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.client.Client;

/** Tests that PluginClient runs actions as its subject and notifies the listener in the caller's context. */
public class PluginClientTests {

    private static final String CALLER_TRANSIENT = "caller.marker";
    private static final String SUBJECT_TRANSIENT = "subject.marker";
    private static final String RESPONDER_TRANSIENT = "responder.marker";

    private static final ActionType<DummyResponse> DUMMY_ACTION = new ActionType<>(
        "cluster:admin/plugin_client_test",
        in -> new DummyResponse()
    );

    private ThreadContext threadContext;
    private Client delegate;
    private PluginClient pluginClient;
    private ExecutorService responder;

    private ActionListener<DummyResponse> capturedListener;
    private String subjectSeenByDelegate;
    private String callerSeenByDelegate;
    private Consumer<ActionListener<DummyResponse>> delegateBehaviour;

    @BeforeEach
    public void setUp() {
        threadContext = new ThreadContext(Settings.EMPTY);
        responder = Executors.newSingleThreadExecutor();

        ThreadPool threadPool = mock(ThreadPool.class);
        when(threadPool.getThreadContext()).thenReturn(threadContext);

        delegate = mock(Client.class);
        when(delegate.settings()).thenReturn(Settings.EMPTY);
        when(delegate.threadPool()).thenReturn(threadPool);

        // Records the context the action ran in and keeps the listener, so each test decides when and
        // how the action completes. By default it does not complete, as for an asynchronous action.
        delegateBehaviour = listener -> {};
        doAnswer(invocation -> {
            subjectSeenByDelegate = threadContext.getTransient(SUBJECT_TRANSIENT);
            callerSeenByDelegate = threadContext.getTransient(CALLER_TRANSIENT);
            capturedListener = invocation.getArgument(2);
            delegateBehaviour.accept(capturedListener);
            return null;
        }).when(delegate).execute(any(ActionType.class), any(ActionRequest.class), any(ActionListener.class));

        pluginClient = new TestPluginClient(delegate);
    }

    @AfterEach
    public void tearDown() {
        responder.shutdownNow();
    }

    @Test
    public void runsTheActionInsideTheSubject() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");
        PluginClient preassigned = new TestPluginClient(delegate, new RecordingSubject(threadContext));

        preassigned.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.noOp());

        assertEquals("set-by-subject", subjectSeenByDelegate, "the action must run inside runAs");
        assertNull(callerSeenByDelegate, "the caller's context must not reach the action");
    }

    @Test
    public void listenerSeesTheCallerContextAndNotTheSubjects() throws Exception {
        assertListenerSeesCallerContext(listener -> listener.onResponse(new DummyResponse()));
    }

    @Test
    public void listenerSeesTheCallerContextOnFailure() throws Exception {
        assertListenerSeesCallerContext(listener -> listener.onFailure(new RuntimeException("action failed")));
    }

    @Test
    public void restoresTheRespondingThreadsContextAfterTheListenerReturns() throws Exception {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");
        assignSubject();
        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.noOp());

        // Restoring the caller's context and leaving it there would strand it on a pooled thread for
        // whatever runs next.
        AtomicReference<String> responderAfter = new AtomicReference<>();
        AtomicReference<String> callerLeftBehind = new AtomicReference<>();
        completeOnAnotherThread(listener -> {
            listener.onResponse(new DummyResponse());
            responderAfter.set(threadContext.getTransient(RESPONDER_TRANSIENT));
            callerLeftBehind.set(threadContext.getTransient(CALLER_TRANSIENT));
        });

        assertEquals("set-by-responder", responderAfter.get(), "responding thread's own context must come back");
        assertNull(callerLeftBehind.get(), "the caller's context must not be left on the responding thread");
    }

    @Test
    public void listenerSeesResponseHeadersTheActionAdded() throws Exception {
        assignSubject();
        AtomicReference<List<String>> warnings = new AtomicReference<>();
        pluginClient
            .execute(
                DUMMY_ACTION,
                new DummyRequest(),
                ActionListener.wrap(r -> warnings.set(threadContext.getResponseHeaders().get("Warning")), e -> {})
            );

        completeOnAnotherThread(listener -> {
            threadContext.addResponseHeader("Warning", "deprecated");
            listener.onResponse(new DummyResponse());
        });

        assertEquals(List.of("deprecated"), warnings.get());
    }

    @Test
    public void throwsADelegateFailureToTheCallerAsTheWrappedClientWould() {
        assignSubject();
        RuntimeException boom = new RuntimeException("failed to find action");
        delegateBehaviour = listener -> { throw boom; };

        List<Exception> failures = new ArrayList<>();
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(r -> {}, failures::add))
        );

        assertSame(boom, thrown);
        assertEquals(List.of(), failures, "a failure thrown before the listener exists must not also reach it");
    }

    @Test
    public void doesNotNotifyAgainWhenTheListenerItselfThrows() {
        assignSubject();
        delegateBehaviour = listener -> listener.onResponse(new DummyResponse());

        RuntimeException listenerBug = new RuntimeException("listener bug");
        AtomicInteger calls = new AtomicInteger();
        ActionListener<DummyResponse> throwing = new ActionListener<>() {
            @Override
            public void onResponse(DummyResponse response) {
                calls.incrementAndGet();
                throw listenerBug;
            }

            @Override
            public void onFailure(Exception e) {
                calls.incrementAndGet();
            }
        };

        RuntimeException surfaced = assertThrows(
            RuntimeException.class,
            () -> pluginClient.execute(DUMMY_ACTION, new DummyRequest(), throwing)
        );
        assertSame(listenerBug, surfaced);
        assertEquals(1, calls.get(), "the listener must be told exactly once");
    }

    @Test
    public void rejectsUseBeforeASubjectIsAssigned() {
        IllegalStateException e = assertThrows(
            IllegalStateException.class,
            () -> pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.noOp())
        );
        assertTrue(e.getMessage().contains("subject"));
    }

    @Test
    public void acceptsTheSameSubjectTwiceButRejectsADifferentOne() {
        RecordingSubject first = assignSubject();
        pluginClient.setSubject(first);

        assertThrows(IllegalStateException.class, () -> pluginClient.setSubject(new RecordingSubject(threadContext)));
    }

    @Test
    public void rejectsANullSubject() {
        assertThrows(NullPointerException.class, () -> pluginClient.setSubject(null));
        assertThrows(NullPointerException.class, () -> new TestPluginClient(delegate, null));
    }

    @Test
    public void refusesARemoteClusterClient() {
        assertThrows(UnsupportedOperationException.class, () -> pluginClient.getRemoteClusterClient("remote"));
    }

    private void assertListenerSeesCallerContext(Consumer<ActionListener<DummyResponse>> completion) throws Exception {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller");
        assignSubject();

        AtomicReference<String> callerSeen = new AtomicReference<>("listener did not run");
        AtomicReference<String> subjectSeen = new AtomicReference<>("listener did not run");
        Consumer<Object> record = ignored -> {
            callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT));
            subjectSeen.set(threadContext.getTransient(SUBJECT_TRANSIENT));
        };
        pluginClient.execute(DUMMY_ACTION, new DummyRequest(), ActionListener.wrap(record::accept, record::accept));

        // A response arrives on a pooled thread that never carried the caller's context, so the wrapper
        // around the listener is the only thing that can put it back.
        completeOnAnotherThread(completion);

        assertEquals("set-by-caller", callerSeen.get());
        assertNull(subjectSeen.get(), "the subject's context must not leak into the listener");
    }

    private RecordingSubject assignSubject() {
        RecordingSubject subject = new RecordingSubject(threadContext);
        pluginClient.setSubject(subject);
        return subject;
    }

    /** Completes the captured action on a thread that never carried the caller's context. */
    private void completeOnAnotherThread(Consumer<ActionListener<DummyResponse>> completion) throws Exception {
        responder.submit(() -> {
            threadContext.putTransient(RESPONDER_TRANSIENT, "set-by-responder");
            completion.accept(capturedListener);
        }).get(10, TimeUnit.SECONDS);
    }

    /**
     * Stands in for the plugin subject. Stashes and restores around its body as NoopPluginSubject and
     * SecurePluginSubject do, and marks the context so a test can tell the subject's apart from the
     * caller's.
     */
    private static class RecordingSubject implements PluginSubject {

        private final ThreadContext threadContext;

        RecordingSubject(ThreadContext threadContext) {
            this.threadContext = threadContext;
        }

        @Override
        public Principal getPrincipal() {
            return () -> "plugin:test";
        }

        @Override
        public <E extends Exception> void runAs(CheckedRunnable<E> runnable) throws E {
            try (ThreadContext.StoredContext ignored = threadContext.stashContext()) {
                threadContext.putTransient(SUBJECT_TRANSIENT, "set-by-subject");
                runnable.run();
            }
        }
    }

    private static final class TestPluginClient extends PluginClient {
        TestPluginClient(Client delegate) {
            super(delegate);
        }

        TestPluginClient(Client delegate, PluginSubject subject) {
            super(delegate, subject);
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
