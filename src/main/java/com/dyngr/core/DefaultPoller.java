/*
 * Copyright 2012-2015 Ye Ding
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dyngr.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dyngr.exception.PollerInterruptedException;
import com.dyngr.Poller;
import com.dyngr.exception.PollerStoppedException;
import com.dyngr.exception.UserBreakException;

/**
 * Default implementation of {@link Poller}.
 *
 * @author dingye
 */
public class DefaultPoller<V> implements Poller<V> {
    private final AtomicBoolean started;
    private final ExecutorService executor;
    private final StopStrategy stopStrategy;
    private final WaitStrategy waitStrategy;
    private final AttemptMaker<V> maker;

    public DefaultPoller(AttemptMaker<V> maker, StopStrategy stopStrategy, WaitStrategy waitStrategy, ExecutorService executor) {
        this.started = new AtomicBoolean(false);
        this.maker = maker;
        this.stopStrategy = stopStrategy;
        this.waitStrategy = waitStrategy;
        this.executor = executor;
    }

    @Override
    public Future<V> start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Poller already started");
        }
        return executor.submit(new PollerCallable<V>(maker, stopStrategy, waitStrategy));
    }

    private static class PollerCallable<V> implements Callable<V> {
        private final AttemptMaker<V> maker;
        private final StopStrategy stopStrategy;
        private final WaitStrategy waitStrategy;

        public PollerCallable(AttemptMaker<V> maker, StopStrategy stopStrategy, WaitStrategy waitStrategy) {
            this.maker = maker;
            this.stopStrategy = stopStrategy;
            this.waitStrategy = waitStrategy;
        }

        @Override
        public V call() throws Exception {
            long startTime = System.currentTimeMillis();
            for (int attemptCount = 1; ;attemptCount++) {
                AttemptResult<V> result;
                try {
                    result = maker.process();
                } catch (Throwable e) {
                    result = AttemptResults.continueFor(e);
                }

                if (result == null) {
                    throw new IllegalStateException("AttemptMaker has returned a null result");
                }

                AttemptState state = result.getState();

                if (state == AttemptState.BREAK) {
                    throw new UserBreakException(result.getMessage(), result.getCause());
                }

                if (state == AttemptState.FINISH) {
                    return result.getResult();
                }

                Attempt failedAttempt = buildAttempt(attemptCount, startTime, System.currentTimeMillis(), result.getCause());
                if (stopStrategy.shouldStop(failedAttempt)) {
                    if (failedAttempt.hasException()) {
                        throw new PollerStoppedException(failedAttempt.getExceptionCause());
                    } else {
                        throw new PollerStoppedException();
                    }
                }

                long waitTime = waitStrategy.computeWaitTime(failedAttempt);

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new PollerInterruptedException(e);
                }
            }
        }

        private Attempt buildAttempt(int attemptNumber, long startTime, long lastEndTime, Throwable cause) {
            return new DefaultAttempt(attemptNumber, startTime, lastEndTime, cause);
        }
    }
}
