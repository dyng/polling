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

import javax.annotation.Nullable;

/**
 * Default implementation of {@link Attempt}.
 *
 * Created by dingye on 17/12/31.
 */
public class DefaultAttempt implements Attempt {
    private final long      attemptNumber;
    private final long      startTime;
    private final long      lastEndTime;
    private final Throwable cause;

    public DefaultAttempt(long attemptNumber, long startTime, long lastEndTime, @Nullable Throwable cause) {
        this.attemptNumber = attemptNumber;
        this.startTime = startTime;
        this.lastEndTime = lastEndTime;
        this.cause = cause;
    }

    @Override
    public long getAttemptNumber() {
        return attemptNumber;
    }

    @Override
    public boolean hasException() {
        return cause != null;
    }

    @Override
    public Throwable getExceptionCause() {
        return cause;
    }

    @Override
    public long getDelaySinceFirstAttempt() {
        return lastEndTime - startTime;
    }
}
