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
package com.dyngr.core.strategy;

import com.dyngr.core.Attempt;
import com.dyngr.core.StopStrategy;
import com.dyngr.util.Preconditions;

/**
 * A stop strategy that stops after a given period elapsed.
 *
 * Created by dingye on 18/1/26.
 */
public class StopAfterDelayStrategy implements StopStrategy {
    private final long maxDelay;

    public StopAfterDelayStrategy(long maxDelay) {
        Preconditions.checkArgument(maxDelay >= 0L, "maxDelay must be >= 0 but is %d", maxDelay);
        this.maxDelay = maxDelay;
    }

    @Override
    public boolean shouldStop(Attempt failedAttempt) {
        return failedAttempt.getDelaySinceFirstAttempt() >= maxDelay;
    }
}
