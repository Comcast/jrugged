package org.fishwife.jrugged;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertTrue;

public class TestConstantFlowThrottler {

    private class DummyCallable implements Callable<String> {

        public String call() throws Exception {
            Thread.sleep(10);
            return "Foo";
        }

    }

    @Test
    public void testInvoke() throws Exception {

        DummyCallable dummy = new DummyCallable();

        ConstantFlowThrottler throttler = new ConstantFlowThrottler();
        throttler.setRequestPerSecondThreshold(60);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 20; i++) {
            throttler.invoke(dummy);
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Total time: " + duration);
        assertTrue(duration > (0.25 * 1000));
        assertTrue(duration < (0.40 * 1000));
    }
}

