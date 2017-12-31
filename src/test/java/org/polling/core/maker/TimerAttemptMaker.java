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
package org.polling.core.maker;

import org.polling.core.AttemptMaker;
import org.polling.core.AttemptResult;
import org.polling.core.AttemptResults;

/**
 * Created by dingye on 17/12/31.
 */
public class TimerAttemptMaker implements AttemptMaker<Void> {
    private volatile Long startAt;
    private volatile Long endAt;

    @Override
    public AttemptResult<Void> process() {
        if (startAt == null) {
            startAt = System.currentTimeMillis();
            return AttemptResults.justContinue();
        } else {
            endAt = System.currentTimeMillis();
            return AttemptResults.justFinish();
        }
    }

    public long getElapsedTime() {
        if (startAt == null || endAt == null) {
            throw new IllegalStateException("Either startAt or endAt is null");
        }
        return endAt - startAt;
    }
}
