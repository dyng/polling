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

package org.polling;

import java.util.concurrent.ExecutorService;

import org.polling.concurrent.DirectExecutorService;
import org.polling.core.AttemptMaker;
import org.polling.core.DefaultPoller;
import org.polling.core.StopStrategies;
import org.polling.core.StopStrategy;
import org.polling.core.WaitStrategies;
import org.polling.core.WaitStrategy;
import org.polling.util.Preconditions;

/**
 * A builder to build a {@link Poller}.
 *
 * @param <V> result type of {@link Poller}'s polling for.
 * @author dingye
 */
public class PollerBuilder<V> {
    private AttemptMaker<V> attemptMaker;
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
     * Sets the body of polling.
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
                stopStrategy == null ? StopStrategies.neverStop() : stopStrategy,
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
}
