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
 * An user defined code which will be executed as the polling body.
 *
 * @author dingye
 */
public interface AttemptMaker<V> {
    /**
     * Do the actual polling action.
     *
     * @return an attempt result
     * @throws Exception any Exception will be caught by poller
     */
    AttemptResult<V> process() throws Exception;
}
