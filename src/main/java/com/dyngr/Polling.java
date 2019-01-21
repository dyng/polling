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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.StopStrategy;
import com.dyngr.core.WaitStrategies;
import com.dyngr.core.WaitStrategy;
import com.dyngr.exception.PollerException;
import com.dyngr.exception.PollerUnknownException;
import com.dyngr.util.Preconditions;

/**
 * A helpful factory that covers most frequently used cases.
 *
 * @author dingye
 */
public class Polling {
    /**
     * Waits with a fixed interval.
     *
     * @param sleepTime the time to sleep
     * @param timeUnit  the unit of the time to sleep
     */
    public static PollingOptions waitPeriodly(long sleepTime, TimeUnit timeUnit) {
        PollingOptions options = newOptions();
        return options.waitPeriodly(sleepTime, timeUnit);
    }

    /**
     * Waits with a random interval.
     *
     * @param maximumTime the maximum time to sleep
     * @param timeUnit    the unit of the maximum time
     */
    public static PollingOptions waitRandomly(long maximumTime, TimeUnit timeUnit) {
        PollingOptions options = newOptions();
        return options.waitRandomly(maximumTime, timeUnit);
    }

    /**
     * Stops after a given delay.
     * @param duration
     * @param timeUnit
     */
    public static PollingOptions stopAfterDelay(long duration, @Nonnull TimeUnit timeUnit) {
        PollingOptions options = newOptions();
        return options.stopAfterDelay(duration, timeUnit);
    }

    /**
     * Stops after N failed attempts.
     * @param attemptNumber
     */
    public static PollingOptions stopAfterAttempt(int attemptNumber) {
        PollingOptions options = newOptions();
        return options.stopAfterAttempt(attemptNumber);
    }

    private static PollingOptions newOptions() {
        return new PollingOptions();
    }

    public static class PollingOptions {
        private Boolean            stopIfException;
        private List<StopStrategy> stopStrategies;
        private List<WaitStrategy> waitStrategies;

        private PollingOptions() {
            stopIfException = null;
            stopStrategies = new ArrayList<StopStrategy>();
            waitStrategies = new ArrayList<WaitStrategy>();
        }

        /**
         * Waits with a fixed interval.
         *
         * @param sleepTime the time to sleep
         * @param timeUnit  the unit of the time to sleep
         */
        public PollingOptions waitPeriodly(long sleepTime, TimeUnit timeUnit) {
            waitStrategies.add(WaitStrategies.fixedWait(sleepTime, timeUnit));
            return this;
        }

        /**
         * Waits with a random interval.
         *
         * @param maximumTime the maximum time to sleep
         * @param timeUnit    the unit of the maximum time
         */
        public PollingOptions waitRandomly(long maximumTime, TimeUnit timeUnit) {
            waitStrategies.add(WaitStrategies.randomWait(maximumTime, timeUnit));
            return this;
        }

        /**
         * Stops after a given delay.
         * @param duration
         * @param timeUnit
         */
        public PollingOptions stopAfterDelay(long duration, @Nonnull TimeUnit timeUnit) {
            stopStrategies.add(StopStrategies.stopAfterDelay(duration, timeUnit));
            return this;
        }

        /**
         * Stops after N failed attempts.
         * @param attemptNumber
         */
        public PollingOptions stopAfterAttempt(int attemptNumber) {
            stopStrategies.add(StopStrategies.stopAfterAttempt(attemptNumber));
            return this;
        }

        /**
         * Should poller stop when an exception is thrown.
         *
         * @param stopIfException whether poller should stop if exception occurred
         */
        public PollingOptions stopIfException(boolean stopIfException) {
            this.stopIfException = stopIfException;
            return this;
        }

        /**
         * Build and start {@link Poller} immediately, waiting for the result.
         *
         * @return the result of polling.
         */
        public <V> V run(AttemptMaker<V> attemptMaker) {
            Preconditions.checkNotNull(attemptMaker, "attemptMake should not be null");
            try {
                return build(attemptMaker).start().get();
            } catch (InterruptedException e) {
                throw new PollerUnknownException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof PollerException) {
                    throw (PollerException) cause;
                } else {
                    throw new PollerUnknownException(e);
                }
            }
        }

        private <V> Poller<V> build(AttemptMaker<V> attemptMaker) {
            PollerBuilder<V> builder = new PollerBuilder<V>();

            if (stopIfException != null) {
                builder.stopIfException(stopIfException);
            }

            if (!stopStrategies.isEmpty()) {
                builder.withStopStrategy(stopStrategies.toArray(new StopStrategy[]{}));
            }

            if (!waitStrategies.isEmpty()) {
                builder.withWaitStrategy(waitStrategies.toArray(new WaitStrategy[]{}));
            }

            builder.polling(attemptMaker);

            return builder.build();
        }
    }
}
