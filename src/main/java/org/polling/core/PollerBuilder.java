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

import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;

/**
 *
 * @author dingye
 * @date 2017/12/22
 */
public class PollerBuilder<V> {
    private AttemptMaker<V> attemptMaker;
    private StopStrategy stopStrategy;
    private WaitStrategy waitStrategy;
    private ExecutorService executorService;

    public PollerBuilder<V> withWaitStrategy(WaitStrategy waitStrategy) {
        Preconditions.checkNotNull(waitStrategy, "waitStrategy should not be null");
        Preconditions.checkState(this.waitStrategy == null, "a waitStrategy has already been set %s", this.waitStrategy);
        this.waitStrategy = waitStrategy;
        return this;
    }

    public PollerBuilder<V> withStopStrategy(StopStrategy stopStrategy) {
        Preconditions.checkNotNull(stopStrategy, "stopStrategy should not be null");
        Preconditions.checkState(this.stopStrategy == null, "a stopStrategy has already been set %s", this.stopStrategy);
        this.stopStrategy = stopStrategy;
        return this;
    }

    public PollerBuilder<V> withExecutorService(ExecutorService executorService) {
        Preconditions.checkNotNull(executorService, "executorService should not be null");
        Preconditions.checkState(this.executorService == null, "a executorService has already been set %s", this.executorService);
        this.stopStrategy = stopStrategy;
        return this;
    }

    public PollerBuilder<V> polling(AttemptMaker<V> attemptMaker) {
        Preconditions.checkNotNull(attemptMaker, "attemptMake should not be null");
        Preconditions.checkState(this.attemptMaker == null, "a attemptMake has already been set %s", this.attemptMaker);
        this.attemptMaker = attemptMaker;
        return this;
    }

    public Poller<V> build() {
        Preconditions.checkNotNull(attemptMaker, "attemptMaker should not be null, please call polling()");
        return new DefaultPoller<V>(
                attemptMaker,
                stopStrategy == null ? StopStrategies.neverStop() : stopStrategy,
                waitStrategy == null ? WaitStrategies.noWait() : waitStrategy,
                executorService == null ? MoreExecutors.newDirectExecutorService() : executorService
        );
    }

    public static <V> PollerBuilder<V> newBuilder() {
        return new PollerBuilder<V>();
    }
}
