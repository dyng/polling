/*
 * Copyright 2012-2015 Ray Holder
 *
 * Modifications copyright 2017 Ye Ding
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.dyngr.core.strategy.CompositeStopStrategy;
import com.dyngr.core.strategy.NeverStopStrategy;
import com.dyngr.core.strategy.StopAfterAttemptStrategy;
import com.dyngr.core.strategy.StopAfterDelayStrategy;
import com.dyngr.util.Preconditions;

/**
 * Factory class for {@link StopStrategy} instances.
 *
 * @author JB
 */
public final class StopStrategies {
    private static final StopStrategy NEVER_STOP = new NeverStopStrategy();

    private StopStrategies() {
    }

    /**
     * Returns a stop strategy which never stops retrying. It might be best to
     * try not to abuse services with this kind of behavior when small wait
     * intervals between retry attempts are being used.
     *
     * @return a stop strategy which never stops
     */
    public static StopStrategy neverStop() {
        return NEVER_STOP;
    }

    /**
     * Returns a stop strategy which stops after N failed attempts.
     *
     * @param attemptNumber the number of failed attempts before stopping
     * @return a stop strategy which stops after {@code attemptNumber} attempts
     */
    public static StopStrategy stopAfterAttempt(int attemptNumber) {
        return new StopAfterAttemptStrategy(attemptNumber);
    }

    /**
     * Returns a stop strategy which stops after a given delay. If an
     * unsuccessful attempt is made, this {@link StopStrategy} will check if the
     * amount of time that's passed from the first attempt has exceeded the
     * given delay amount. If it has exceeded this delay, then using this
     * strategy causes the retrying to stop.
     *
     * @param duration the delay, starting from first attempt
     * @param timeUnit the unit of the duration
     * @return a stop strategy which stops after {@code delayInMillis} time in milliseconds
     */
    public static StopStrategy stopAfterDelay(long duration, @Nonnull TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "The time unit may not be null");
        return new StopAfterDelayStrategy(timeUnit.toMillis(duration));
    }

    /**
     * Joins one or more stop strategies to derive a composite stop strategy.
     * The new joined strategy will stop if any underlying stop strategy says so.
     *
     * @param  stopStrategies stop strategies that need to be checked one by one.
     * @return A composite wait strategy
     */
    public static StopStrategy join(StopStrategy... stopStrategies) {
        Preconditions.checkState(stopStrategies.length > 0, "Must have at least one stop strategy");
        List<StopStrategy> stopStrategyList = new ArrayList<StopStrategy>(Arrays.asList(stopStrategies));
        Preconditions.checkState(!stopStrategyList.contains(null), "Cannot have a null stop strategy");
        return new CompositeStopStrategy(stopStrategyList);
    }
}
