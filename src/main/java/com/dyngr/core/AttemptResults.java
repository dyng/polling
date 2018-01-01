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

import com.dyngr.exception.UserBreakException;

/**
 * Factory class for instances of {@link AttemptResult}.
 *
 * @author dingye
 */
public final class AttemptResults {
    /**
     * Return an {@link AttemptResult} that will end polling without result.
     *
     * @return an attempt result
     */
    public static AttemptResult<Void> justFinish() {
        return new AttemptResult<Void>(AttemptState.FINISH, null, "", null);
    }

    /**
     * Return an {@link AttemptResult} that will end polling with given result.
     *
     * @param result result of a successful attempt
     * @param <V>  return type of poller
     * @return an attempt result
     */
    public static <V> AttemptResult<V> finishWith(V result) {
        return new AttemptResult<V>(AttemptState.FINISH, result, "", null);
    }

    /**
     * Return an {@link AttemptResult} that will break polling and throw an {@link UserBreakException}.
     *
     * @param message message about reason
     * @param <V>  return type of poller
     * @return an attempt result
     */
    public static <V> AttemptResult<V> breakFor(String message) {
        return new AttemptResult<V>(AttemptState.BREAK, null, message, null);
    }

    /**
     * Return an {@link AttemptResult} that will break polling and throw an {@link UserBreakException}.
     *
     * @param cause reason of user break
     * @param <V>  return type of poller
     * @return an attempt result
     */
    public static <V> AttemptResult<V> breakFor(Throwable cause) {
        return new AttemptResult<V>(AttemptState.BREAK, null, cause.getMessage(), cause);
    }

    /**
     * Return an {@link AttemptResult} that makes polling continue.
     *
     * @param <V> return type of poller
     * @return an attempt result
     */
    public static <V> AttemptResult<V> justContinue() {
        return new AttemptResult<V>(AttemptState.CONTINUE, null, "", null);
    }

    public static <V> AttemptResult<V> continueFor(Throwable cause) {
        return new AttemptResult<V>(AttemptState.CONTINUE, null, "", cause);
    }
}
