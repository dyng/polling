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
package com.dyngr.util;

import java.util.concurrent.ExecutionException;

import com.dyngr.exception.PollerInterruptedException;
import com.dyngr.exception.PollerStoppedException;

/**
 * Utility class to analyze exceptions thrown during polling processing.
 *
 * @author dingye
 */
public class PollerExceptions {
    /**
     * Tells whether the polling is stopped due to a wait strategy condition met.
     * @param throwable a thrown exception.
     */
    public static boolean isStopped(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        if (throwable instanceof ExecutionException) {
            return isStopped(throwable.getCause());
        }

        if (throwable instanceof PollerStoppedException) {
            return true;
        }

        return false;
    }

    /**
     * Tells whether the polling is stopped due to a interruption.
     * @param throwable a thrown exception.
     */
    public static boolean isInterrupted(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        if (throwable instanceof ExecutionException) {
            return isInterrupted(throwable.getCause());
        }

        if (throwable instanceof PollerInterruptedException ||
                throwable instanceof InterruptedException) {
            return true;
        }

        return false;
    }
}
