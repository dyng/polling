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
package com.dyngr.exception;

/**
 * An exception which implies the worker thread is interrupted during polling.
 *
 * Created by dingye on 17/12/24.
 */
public class PollerInterruptedException extends PollerException {
    public PollerInterruptedException() {
    }

    public PollerInterruptedException(String message) {
        super(message);
    }

    public PollerInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PollerInterruptedException(Throwable cause) {
        super(cause);
    }
}
