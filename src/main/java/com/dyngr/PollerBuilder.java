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

package com.dyngr;

import java.util.concurrent.ExecutorService;

import com.dyngr.concurrent.DirectExecutorService;
import com.dyngr.core.Attempt;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.DefaultPoller;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.StopStrategy;
import com.dyngr.core.WaitStrategies;
import com.dyngr.core.WaitStrategy;
import com.dyngr.util.Preconditions;

/**
 * A builder to build a {@link Poller}.
 *
 * @param <V> result type of {@link Poller}'s polling for.
 * @author dingye
 */
public class PollerBuilder<V> {
    private AttemptMaker<V> attemptMaker;
    private Boolean         stopIfException;
    private StopStrategy    stopStrategy;
    private WaitStrategy    waitStrategy;
    private ExecutorService executorService;

    /**
     * Sets the wait strategy used to decide how long to sleep between failed attempts.
     * The default strategy is to retry immediately after a failed attempt.
     *
     * @param waitStrategy the strategy used to sleep between failed attempts
     * @return <code>this</code>
     * @throws IllegalStateException if a wait strategy has already been set.
     */
    public PollerBuilder<V> withWaitStrategy(WaitStrategy waitStrategy) {
        Preconditions.checkNotNull(waitStrategy, "waitStrategy should not be null");
        Preconditions.checkState(this.waitStrategy == null, "a waitStrategy has already been set %s", this.waitStrategy);
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * Sets a list of wait strategy used to decide how long to sleep between failed attempts. Actual wait time will be a total of all computed wait time.
     *
     * @param waitStrategies list of strategies used to sleep between failed attempts
     * @return <code>this</code>
     * @throws IllegalStateException if a wait strategy has already been set.
     */
    public PollerBuilder<V> withWaitStrategy(WaitStrategy... waitStrategies) {
        Preconditions.checkNotNull(waitStrategies, "waitStrategy should not be null");
        for (WaitStrategy strategy : waitStrategies) {
            Preconditions.checkNotNull(strategy, "waitStrategy should not be null");
        }
        this.waitStrategy = WaitStrategies.join(waitStrategies);
        return this;
    }

    /**
     * Set if poller should stop when an exception is thrown. The default value is <code>true</code>.
     *
     * @param stopIfException whether poller should stop if exception occurred
     * @return <code>this</code>
     */
    public PollerBuilder<V> stopIfException(boolean stopIfException) {
        this.stopIfException = stopIfException;
        return this;
    }

    /**
     * Sets the stop strategy used to decide when to stop retrying. The default strategy is to not stop at all .
     *
     * @param stopStrategy the strategy used to decide when to stop retrying
     * @return <code>this</code>
     * @throws IllegalStateException if a stop strategy has already been set.
     */
    public PollerBuilder<V> withStopStrategy(StopStrategy stopStrategy) {
        Preconditions.checkNotNull(stopStrategy, "stopStrategy should not be null");
        Preconditions.checkState(this.stopStrategy == null, "a stopStrategy has already been set %s", this.stopStrategy);
        this.stopStrategy = stopStrategy;
        return this;
    }

    /**
     * Sets a list of stop strategies used to decide when to stop retrying. polling will stop if any stop strategy's condition is fulfilled.
     *
     * @param stopStrategies list of strategies used to decide when to stop retrying.
     * @return <code>this</code>
     * @throws IllegalStateException if a stop strategy has already been set.
     */
    public PollerBuilder<V> withStopStrategy(StopStrategy... stopStrategies) {
        Preconditions.checkState(this.stopStrategy == null, "a stopStrategy has already been set %s", this.stopStrategy);
        for (StopStrategy strategy : stopStrategies) {
            Preconditions.checkNotNull(strategy, "stopStrategy should not be null");
        }
        this.stopStrategy = StopStrategies.join(stopStrategies);
        return this;
    }

    /**
     * Sets the {@link ExecutorService} on which {@link Poller} will be running. The default is on the thread that calls {@link Poller#start()}.
     *
     * @param executorService the executor service which is used to do the polling.
     * @return <code>this</code>
     * @throws IllegalStateException if an executor service has already been set.
     */
    public PollerBuilder<V> withExecutorService(ExecutorService executorService) {
        Preconditions.checkNotNull(executorService, "executorService should not be null");
        Preconditions.checkState(this.executorService == null, "a executorService has already been set %s", this.executorService);
        this.executorService = executorService;
        return this;
    }

    /**
     * Sets the code of actual polling.
     *
     * @param attemptMaker the polling body.
     * @return <code>this</code>
     * @throws IllegalArgumentException if an attempt maker has already been set.
     */
    public PollerBuilder<V> polling(AttemptMaker<V> attemptMaker) {
        Preconditions.checkNotNull(attemptMaker, "attemptMake should not be null");
        Preconditions.checkState(this.attemptMaker == null, "a attemptMake has already been set %s", this.attemptMaker);
        this.attemptMaker = attemptMaker;
        return this;
    }

    /**
     * Finally build the {@link Poller} instance.
     *
     * @return the built poller.
     */
    public Poller<V> build() {
        Preconditions.checkNotNull(attemptMaker, "attemptMaker should not be null, please call polling() to add a AttemptMaker");
        return new DefaultPoller<V>(
                attemptMaker,
                buildStopStrategy(),
                waitStrategy == null ? WaitStrategies.noWait() : waitStrategy,
                executorService == null ? new DirectExecutorService() : executorService
        );
    }

    /**
     * Constructs a new builder
     *
     * @param <V> result type of {@link Poller}'s polling for.
     * @return the new builder
     */
    public static <V> PollerBuilder<V> newBuilder() {
        return new PollerBuilder<V>();
    }

    /**
     * Build a stop strategy based on user defined ones.
     */
    private StopStrategy buildStopStrategy() {
        if (stopStrategy == null) {
            stopStrategy = StopStrategies.neverStop();
        }

        if (stopIfException == null) {
            stopIfException = true;
        }

        return new MayStopIfExceptionStopStrategy(stopIfException, stopStrategy);
    }

    /**
     * A wrapper stop strategy that may or may not stop if exception occurred.
     */
    private static class MayStopIfExceptionStopStrategy implements StopStrategy {
        private final boolean stopIfException;
        private final StopStrategy stopStrategy;

        public MayStopIfExceptionStopStrategy(boolean stopIfException, StopStrategy stopStrategy) {
            this.stopIfException = stopIfException;
            this.stopStrategy = stopStrategy;
        }

        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            if (stopIfException && failedAttempt.hasException()) {
                return true;
            } else {
                return stopStrategy.shouldStop(failedAttempt);
            }
        }
    }
}
