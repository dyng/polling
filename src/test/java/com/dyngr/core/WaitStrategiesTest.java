/*
 * Copyright 2012-2015 Ray Holder
 * 
 * Modifications copyright 2017 Ye Ding
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dingye on 17/12/31.
 */
public class WaitStrategiesTest {


    @Test
    public void testNoWait() {
        WaitStrategy noWait = WaitStrategies.noWait();
        assertEquals(0L, noWait.computeWaitTime(failedAttempt(18, 9879L)));
    }

    @Test
    public void testFixedWait() {
        WaitStrategy fixedWait = WaitStrategies.fixedWait(1000L, TimeUnit.MILLISECONDS);
        assertEquals(1000L, fixedWait.computeWaitTime(failedAttempt(12, 6546L)));
    }

    @Test
    public void testIncrementingWait() {
        WaitStrategy incrementingWait = WaitStrategies.incrementingWait(500L, TimeUnit.MILLISECONDS, 100L, TimeUnit.MILLISECONDS);
        assertEquals(500L, incrementingWait.computeWaitTime(failedAttempt(1, 6546L)));
        assertEquals(600L, incrementingWait.computeWaitTime(failedAttempt(2, 6546L)));
        assertEquals(700L, incrementingWait.computeWaitTime(failedAttempt(3, 6546L)));
    }

    @Test
    public void testRandomWait() {
        WaitStrategy randomWait = WaitStrategies.randomWait(1000L, TimeUnit.MILLISECONDS, 2000L, TimeUnit.MILLISECONDS);
        Set<Long> times = new HashSet<Long>();
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        assertTrue(times.size() > 1); // if not, the random is not random
        for (long time : times) {
            assertTrue(time >= 1000L);
            assertTrue(time <= 2000L);
        }
    }

    @Test
    public void testRandomWaitWithoutMinimum() {
        WaitStrategy randomWait = WaitStrategies.randomWait(2000L, TimeUnit.MILLISECONDS);
        Set<Long> times = new HashSet<Long>();
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        times.add(randomWait.computeWaitTime(failedAttempt(1, 6546L)));
        assertTrue(times.size() > 1); // if not, the random is not random
        for (long time : times) {
            assertTrue(time >= 0L);
            assertTrue(time <= 2000L);
        }
    }

    @Test
    public void testExponential() {
        WaitStrategy exponentialWait = WaitStrategies.exponentialWait();
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(1, 0)) == 2);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(2, 0)) == 4);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(3, 0)) == 8);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(4, 0)) == 16);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(5, 0)) == 32);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(6, 0)) == 64);
    }

    @Test
    public void testExponentialWithMaximumWait() {
        WaitStrategy exponentialWait = WaitStrategies.exponentialWait(40, TimeUnit.MILLISECONDS);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(1, 0)) == 2);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(2, 0)) == 4);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(3, 0)) == 8);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(4, 0)) == 16);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(5, 0)) == 32);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(6, 0)) == 40);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(7, 0)) == 40);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(Integer.MAX_VALUE, 0)) == 40);
    }

    @Test
    public void testExponentialWithMultiplierAndMaximumWait() {
        WaitStrategy exponentialWait = WaitStrategies.exponentialWait(1000, 50000, TimeUnit.MILLISECONDS);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(1, 0)) == 2000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(2, 0)) == 4000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(3, 0)) == 8000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(4, 0)) == 16000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(5, 0)) == 32000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(6, 0)) == 50000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(7, 0)) == 50000);
        assertTrue(exponentialWait.computeWaitTime(failedAttempt(Integer.MAX_VALUE, 0)) == 50000);
    }

    @Test
    public void testFibonacci() {
        WaitStrategy fibonacciWait = WaitStrategies.fibonacciWait();
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(1, 0L)) == 1L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(2, 0L)) == 1L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(3, 0L)) == 2L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(4, 0L)) == 3L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(5, 0L)) == 5L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(6, 0L)) == 8L);
    }

    @Test
    public void testFibonacciWithMaximumWait() {
        WaitStrategy fibonacciWait = WaitStrategies.fibonacciWait(10L, TimeUnit.MILLISECONDS);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(1, 0L)) == 1L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(2, 0L)) == 1L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(3, 0L)) == 2L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(4, 0L)) == 3L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(5, 0L)) == 5L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(6, 0L)) == 8L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(7, 0L)) == 10L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(Integer.MAX_VALUE, 0L)) == 10L);
    }

    @Test
    public void testFibonacciWithMultiplierAndMaximumWait() {
        WaitStrategy fibonacciWait = WaitStrategies.fibonacciWait(1000L, 50000L, TimeUnit.MILLISECONDS);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(1, 0L)) == 1000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(2, 0L)) == 1000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(3, 0L)) == 2000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(4, 0L)) == 3000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(5, 0L)) == 5000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(6, 0L)) == 8000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(7, 0L)) == 13000L);
        assertTrue(fibonacciWait.computeWaitTime(failedAttempt(Integer.MAX_VALUE, 0L)) == 50000L);
    }

    @Test
    public void testExceptionWait() {
        WaitStrategy exceptionWait = WaitStrategies.exceptionWait(RuntimeException.class, zeroSleepFunction());
        assertEquals(0L, exceptionWait.computeWaitTime(failedAttempt(42, 7227)));

        WaitStrategy oneMinuteWait = WaitStrategies.exceptionWait(RuntimeException.class, oneMinuteSleepFunction());
        assertEquals(3600 * 1000L, oneMinuteWait.computeWaitTime(failedAttempt(42, 7227)));

        WaitStrategy noMatchRetryAfterWait = WaitStrategies.exceptionWait(RetryAfterException.class, customSleepFunction());
        assertEquals(0L, noMatchRetryAfterWait.computeWaitTime(failedAttempt(42, 7227)));

        WaitStrategy retryAfterWait = WaitStrategies.exceptionWait(RetryAfterException.class, customSleepFunction());
        assertEquals(29L, retryAfterWait.computeWaitTime(failedRetryAfterAttempt(42, 7227)));
    }

    public Attempt failedAttempt(long attemptNumber, long delaySinceFirstAttempt) {
        return new DefaultAttempt(attemptNumber, 0L, delaySinceFirstAttempt, new RuntimeException());
    }

    public Attempt failedRetryAfterAttempt(long attemptNumber, long delaySinceFirstAttempt) {
        return new DefaultAttempt(attemptNumber, 0L, delaySinceFirstAttempt, new RetryAfterException());
    }

    public ExceptionWaitHandler<RuntimeException> zeroSleepFunction() {
        return new ExceptionWaitHandler<RuntimeException>() {
            @Override
            public long computeWaitTime(RuntimeException throwable) {
                return 0L;
            }
        };
    }

    public ExceptionWaitHandler<RuntimeException> oneMinuteSleepFunction() {
        return new ExceptionWaitHandler<RuntimeException>() {
            @Override
            public long computeWaitTime(RuntimeException throwable) {
                return 3600 * 1000L;
            }
        };
    }

    public ExceptionWaitHandler<RetryAfterException> customSleepFunction() {
        return new ExceptionWaitHandler<RetryAfterException>() {
            @Override
            public long computeWaitTime(RetryAfterException throwable) {
                return throwable.getRetryAfter();
            }
        };
    }

    public class RetryAfterException extends RuntimeException {
        private final long retryAfter = 29L;

        public long getRetryAfter() {
            return retryAfter;
        }
    }
}
