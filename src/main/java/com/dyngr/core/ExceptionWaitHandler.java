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

/**
 * An handler to compute next waiting time based on caught exception.
 *
 * Created by dingye on 17/12/24.
 */
public interface ExceptionWaitHandler<T extends Throwable> {
    /**
     * Returns the time, in milliseconds, to sleep before retrying.
     *
     * @param throwable exception that is thrown.
     * @return the sleep time before next attempt
     */
    long computeWaitTime(T throwable);
}
