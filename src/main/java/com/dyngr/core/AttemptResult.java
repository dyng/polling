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
 * Result of a single attempt.
 * <p></p>
 * As a rule of thumb, always use {@link AttemptResults} to create {@link AttemptResult}.
 *
 * @author dingye
 */
public class AttemptResult<V> {
    private final AttemptState state;
    private final V result;
    private final String message;
    private final Throwable cause;

    AttemptResult(AttemptState state, V result, String message, @Nullable Throwable cause) {
        this.state = state;
        this.result = result;
        this.message = message;
        this.cause = cause;
    }

    public AttemptState getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public V getResult() {
        return result;
    }

    public Throwable getCause() {
        return cause;
    }
}
