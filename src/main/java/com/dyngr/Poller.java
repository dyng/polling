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

package com.dyngr;

import java.util.concurrent.Future;

import com.dyngr.core.StopStrategy;
import com.dyngr.core.WaitStrategy;

/**
 * A poller will do some work periodically, and stops if a {@link StopStrategy} is fulfilled.
 * The interval of every single execution is determined by {@link WaitStrategy}.
 *
 * @param <V> the type of result
 * @author dingye
 */
public interface Poller<V> {
    /**
     * Start this poller.
     *
     * @return future of polling result
     */
    Future<V> start();
}
