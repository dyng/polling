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
 * A wrapper runtime exception for those unknown exceptions.
 *
 * Created by dingye on 19/1/19.
 */
public class PollerUnknownException extends PollerException {
    public PollerUnknownException() {
    }

    public PollerUnknownException(String message) {
        super(message);
    }

    public PollerUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public PollerUnknownException(Throwable cause) {
        super(cause);
    }
}
