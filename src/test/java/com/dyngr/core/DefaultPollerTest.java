package com.dyngr.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.dyngr.core.maker.CounterAttemptMaker;
import com.dyngr.exception.PollerInterruptedException;
import org.junit.Test;
import com.dyngr.Poller;
import com.dyngr.PollerBuilder;
import com.dyngr.core.maker.TimerAttemptMaker;
import com.dyngr.core.maker.TryFixedTimesAttemptMaker;
import com.dyngr.exception.PollerStoppedException;
import com.dyngr.exception.UserBreakException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by dingye on 17/12/31.
 */
public class DefaultPollerTest {
    private static final ExecutorService runner = Executors.newFixedThreadPool(1);

    @Test
    public void testStart_already_started() throws Exception {
        // prepare
        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .polling(new TryFixedTimesAttemptMaker(0))
                .build();
        poller.start().get();

        // verify
        try {
            poller.start();
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e).hasMessage("Poller already started");
        }
    }

    @Test
    public void testStart_on_current_thread() throws Exception {
        // prepare
        final AtomicLong runningThreadId = new AtomicLong();

        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .polling(new AttemptMaker<Void>() {
                    @Override
                    public AttemptResult<Void> process() {
                        runningThreadId.set(Thread.currentThread().getId());
                        return AttemptResults.justFinish();
                    }
                })
                .build();

        // verify
        poller.start().get();
        assertThat(runningThreadId.get())
                .isNotNull()
                .isEqualTo(Thread.currentThread().getId());
    }

    @Test
    public void testStart_on_another_thread() throws Exception {
        // prepare
        final AtomicLong runningThreadId = new AtomicLong();

        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .withExecutorService(runner)
                .polling(new AttemptMaker<Void>() {
                    @Override
                    public AttemptResult<Void> process() {
                        runningThreadId.set(Thread.currentThread().getId());
                        return AttemptResults.justFinish();
                    }
                })
                .build();

        // verify
        poller.start().get();
        assertThat(runningThreadId.get())
                .isNotNull()
                .isNotEqualTo(Thread.currentThread().getId());
    }

    @Test
    public void testGetPollingResult() throws Exception {
        // prepare
        Poller<String> poller = PollerBuilder.<String>newBuilder()
                .polling(new AttemptMaker<String>() {
                    @Override
                    public AttemptResult<String> process() {
                        return AttemptResults.finishWith("hello, world!");
                    }
                })
                .build();

        // verify
        String result = poller.start().get();
        assertThat(result)
                .isNotNull()
                .isEqualTo("hello, world!");
    }

    @Test
    public void testStopStrategy_takes_effect() throws Exception {
        // prepare
        CounterAttemptMaker attemptMaker = new CounterAttemptMaker();
        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .polling(attemptMaker)
                .build();

        // verify
        try {
            poller.start().get();
        } catch (ExecutionException e) {
            assertThat(e.getCause())
                    .isInstanceOf(PollerStoppedException.class);
        }

        assertThat(attemptMaker.getCount())
                .isEqualTo(3);
    }

    @Test
    public void testWaitStrategy_takes_effect() throws Exception {
        // prepare
        TimerAttemptMaker attemptMaker = new TimerAttemptMaker();
        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .withWaitStrategy(WaitStrategies.fixedWait(200, TimeUnit.MILLISECONDS))
                .polling(attemptMaker)
                .build();

        // verify
        poller.start().get();
        assertThat(attemptMaker.getElapsedTime())
                .isGreaterThanOrEqualTo(200);
    }

    @Test
    public void testAttemptResult_user_break() throws Exception {
        // prepare
        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .polling(new AttemptMaker<Void>() {
                    @Override
                    public AttemptResult<Void> process() {
                        return AttemptResults.breakFor(new IllegalStateException("oops!"));
                    }
                })
                .build();

        // verify
        try {
            poller.start().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause())
                    .isInstanceOf(UserBreakException.class);
            assertThat(e.getCause().getCause())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("oops!");
        }
    }

    @Test
    public void testAttemptResult_unexpected_exception() throws Exception {
        // prepare
        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .withStopStrategy(StopStrategies.stopIfException())
                .polling(new AttemptMaker<Void>() {
                    @Override
                    public AttemptResult<Void> process() {
                        throw new IllegalStateException();
                    }
                })
                .build();

        // verify
        try {
            poller.start().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause())
                    .isInstanceOf(PollerStoppedException.class);
        }
    }

    @Test
    public void testPollerInterrupted() throws Exception {
        // prepare
        final AtomicReference<Thread> worker = new AtomicReference<Thread>();

        Poller<Void> poller = PollerBuilder.<Void>newBuilder()
                .withExecutorService(runner)
                .withWaitStrategy(new WaitStrategy() {
                    @Override
                    public long computeWaitTime(Attempt failedAttempt) {
                        return Long.MAX_VALUE;
                    }
                })
                .polling(new AttemptMaker<Void>() {
                    @Override
                    public AttemptResult<Void> process() {
                        worker.set(Thread.currentThread());
                        return AttemptResults.justContinue();
                    }
                })
                .build();

        // verify
        Future<Void> future = poller.start();
        Thread.sleep(300);
        while (worker.get() == null) {
            // spin
        }

        // interrupt sleeping
        worker.get().interrupt();

        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause())
                    .isInstanceOf(PollerInterruptedException.class);
        }
    }
}