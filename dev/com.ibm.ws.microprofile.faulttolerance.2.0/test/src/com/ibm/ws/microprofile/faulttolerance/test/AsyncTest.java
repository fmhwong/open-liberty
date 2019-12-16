/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.faulttolerance.test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.junit.Test;

import com.ibm.ws.microprofile.faulttolerance.spi.Executor;
import com.ibm.ws.microprofile.faulttolerance.spi.ExecutorBuilder;
import com.ibm.ws.microprofile.faulttolerance.spi.FaultToleranceProvider;
import com.ibm.ws.microprofile.faulttolerance.spi.RetryPolicy;
import com.ibm.ws.microprofile.faulttolerance.test.util.AsyncTestFunction;
import com.ibm.ws.microprofile.faulttolerance.test.util.MockAsyncRequestContext;
import com.ibm.ws.microprofile.faulttolerance.test.util.TestException;

/**
 *
 */
public class AsyncTest extends AbstractFTTest {

    private static final int TASKS = 5;
    private static final long DURATION_UNIT = 1000;
    private static final long TASK_DURATION = 4 * DURATION_UNIT;
    private static final long FUTURE_TIMEOUT = 2 * TASK_DURATION;

    @Test
    public void testAsync() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();
        Executor<Future<String>> executor = builder.buildAsync(Future.class);

        List<Future<String>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < TASKS; i++) {
                String id = "testAsync" + i;
                AsyncTestFunction callable = new AsyncTestFunction(Duration.ofMillis(TASK_DURATION), id);
                ExecutionContext context = executor.newExecutionContext(id, (Method) null, id);
                Future<String> future = executor.execute(callable, context);
                assertFalse(future.isDone());
                futures.add(future);
            }

            for (int i = 0; i < TASKS; i++) {
                String data = futures.get(i).get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                assertEquals("testAsync" + i, data);
            }
        } finally {
            for (int i = 0; i < TASKS; i++) {
                Future<String> future = futures.get(i);
                if (future != null && !future.isDone()) {
                    future.cancel(true);
                }
            }
        }
    }

    @Test
    public void testAsyncCancel() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();
        Executor<Future<String>> executor = builder.buildAsync(Future.class);
        ExecutionContext context = executor.newExecutionContext("testAsyncCancellation", null);

        CompletableFuture<Void> runningFuture = new CompletableFuture<>();
        CompletableFuture<Void> interruptedFuture = new CompletableFuture<>();

        // Start a task that sleeps, waiting to be interrupted
        Future<String> result = executor.execute(() -> {
            runningFuture.complete(null);
            try {
                Thread.sleep(FUTURE_TIMEOUT);
            } catch (InterruptedException e) {
                interruptedFuture.complete(null);
            }
            return CompletableFuture.completedFuture("OK");
        }, context);

        // Wait for it to start running
        runningFuture.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);

        // Now cancel it
        assertThat("Calling cancel returned wrong result", result.cancel(true), is(true));

        // Assert we get the correct response from the result object
        assertThat("Result did not report done", result.isDone(), is(true));
        assertThat("Result did not report cancelled", result.isCancelled(), is(true));
        try {
            result.get(0, TimeUnit.SECONDS);
            fail("result.get did not throw CancellationException");
        } catch (CancellationException e) {
            // Expected
        }

        // Check that the running task is actually interrupted
        try {
            interruptedFuture.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            fail("Running task was not interrupted when future was cancelled");
        }

        // Give it a moment to finish
        Thread.sleep(DURATION_UNIT);

        // Check that the result still gives the correct response after the cancelled execution has finished successfully
        assertThat("Result does not report done", result.isDone(), is(true));
        assertThat("Result does not report cancelled", result.isCancelled(), is(true));
        try {
            result.get(0, TimeUnit.SECONDS);
            fail("result.get did not throw CancellationException");
        } catch (CancellationException e) {
            // Expected
        }
    }

    @Test
    public void testAsyncCS() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();
        Executor<CompletionStage<String>> executor = builder.buildAsync(CompletionStage.class);
        ExecutionContext context = executor.newExecutionContext("testAsyncCS", null);

        CompletableFuture<String> waitingFuture = new CompletableFuture<>();

        CompletionStage<String> result = executor.execute(this::waitThenReturnCS, context);
        result.thenAccept((r) -> {
            waitingFuture.complete(r);
        });

        assertFalse("Waiting future is done", waitingFuture.isDone());
        waitingFuture.get(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testAsyncRequestContextCalls() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();

        MockAsyncRequestContext asyncRequestContext = new MockAsyncRequestContext();
        builder.setRequestContext(asyncRequestContext);

        Executor<CompletionStage<String>> executor = builder.buildAsync(CompletionStage.class);
        ExecutionContext context = executor.newExecutionContext("testAsyncCS", null);

        assertEquals(0, asyncRequestContext.getActivateContextCount());
        assertEquals(0, asyncRequestContext.getDeactivateContextCount());

        CompletionStage<String> result = executor.execute(() -> {
            assertEquals("Activate request context should be called when request is started", 1, asyncRequestContext.getActivateContextCount());
            assertEquals("Deactivate request context should only be called when request is finished", 0, asyncRequestContext.getDeactivateContextCount());
            return CompletableFuture.completedFuture("Test");
        }, context);

        result.toCompletableFuture().get(2000, TimeUnit.MILLISECONDS);

        assertEquals("Deactivate request context should be called when request is finished", 1, asyncRequestContext.getDeactivateContextCount());
    }

    @Test
    public void testAsyncRequestContextDeactivatedOnInterruption() throws InterruptedException, ExecutionException, TimeoutException, TestException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();

        MockAsyncRequestContext asyncRequestContext = new MockAsyncRequestContext();
        builder.setRequestContext(asyncRequestContext);

        Executor<Future<String>> executor = builder.buildAsync(CompletionStage.class);
        ExecutionContext context = executor.newExecutionContext("testAsyncCS", null);

        // Test an execution which throws an exception
        Future<String> result = executor.execute(() -> {
            throw new TestException();
        }, context);

        try {
            // Give the execution up to 5 seconds to complete
            result.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(TestException.class));
            assertEquals("Activate request context should have been called", 1, asyncRequestContext.getActivateContextCount());
            assertEquals("Deactivate request context should still be called if method throws an exeption", 1, asyncRequestContext.getDeactivateContextCount());
        } catch (TimeoutException e) {
            throw e;
        }
    }

    @Test
    public void testAsyncRequestContextWithRetry() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorBuilder<String> builder = FaultToleranceProvider.newExecutionBuilder();

        RetryPolicy retry = FaultToleranceProvider.newRetryPolicy();
        retry.setMaxRetries(3);
        builder.setRetryPolicy(retry);

        MockAsyncRequestContext asyncRequestContext = new MockAsyncRequestContext();
        builder.setRequestContext(asyncRequestContext);

        Executor<Future<String>> executor = builder.buildAsync(CompletionStage.class);
        ExecutionContext context = executor.newExecutionContext("testAsyncCS", null);

        Future<String> result = executor.execute(() -> {
            throw new TestException();
        }, context);

        try {
            // Give the execution up to 5 seconds to complete
            result.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(TestException.class));
            assertEquals("Activate request context should be called 4 times", 4, asyncRequestContext.getActivateContextCount());
            assertEquals("Deactivate request context should be called 4 times", 4, asyncRequestContext.getDeactivateContextCount());
        } catch (TimeoutException e) {
            throw e;
        }
    }

    private CompletionStage<String> waitThenReturnCS() throws InterruptedException {
        Thread.sleep(1000);
        return CompletableFuture.completedFuture("Test");
    }

}
