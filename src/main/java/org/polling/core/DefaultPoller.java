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

package org.polling.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author dingye
 * @date 2017/12/22
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
            throw new IllegalStateException("Poller already started.");
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
            long startTime = System.nanoTime();
            for (int attemptCount = 1; ;attemptCount++) {
                AttemptResult<V> result;
                try {
                    result = maker.process();
                } catch (Exception e) {
                    result = AttemptResults.continueFor(e);
                }

                AttemptState state = result.getState();

                if (state == AttemptState.BREAK) {
                    // TODO: 更好的异常
                    throw new RuntimeException("UserException. Message: " + result.getMessage());
                }

                if (state == AttemptState.COMPLETE) {
                    return result.getResult();
                }

                Attempt failedAttempt = buildAttempt(attemptCount, startTime, System.nanoTime(), result.getCause());
                if (stopStrategy.shouldStop(failedAttempt)) {
                    // TODO: 更好的异常
                    throw new RuntimeException("StopStrategy makes further polling stopped.");
                }

                long waitTime = waitStrategy.computeWaitTime(failedAttempt);

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // TODO: 更好的异常
                    throw new RuntimeException("Thread interrupted in waiting.");
                }
            }
        }

        private Attempt buildAttempt(int attemptNumber, long startTime, long lastEndTime, Throwable cause) {
            return new Attempt(attemptNumber, startTime, lastEndTime, cause);
        }
    }
}
