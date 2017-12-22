package org.polling.core;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author dingye
 * @date 2017/12/22
 */
public class PollerBuilderTest {
    @Test
    public void test() throws Exception {
        Poller<String> poller = PollerBuilder.<String>newBuilder()
                .polling(new AttemptMaker<String>() {
                    @Override
                    public AttemptResult<String> process() {
                        System.out.println("hello");
                        return AttemptResults.completeWith("hello");
                    }
                })
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withWaitStrategy(WaitStrategies.fixedWait(200, TimeUnit.MILLISECONDS))
                .build();

        try {
            String result = poller.start().get();
            assertThat(result).isEqualTo("hello");
        } catch (RuntimeException e) {

        }
    }
}