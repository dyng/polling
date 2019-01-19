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

import com.dyngr.core.StopStrategy;

/**
 * An exception implies polling is stopped by a {@link StopStrategy}.
 *
 * Created by dingye on 17/12/24.
 */
public class PollerStoppedException extends PollerException {
    public PollerStoppedException() {
    }

    public PollerStoppedException(String message) {
        super(message);
    }

    public PollerStoppedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PollerStoppedException(Throwable cause) {
        super(cause);
    }
}
