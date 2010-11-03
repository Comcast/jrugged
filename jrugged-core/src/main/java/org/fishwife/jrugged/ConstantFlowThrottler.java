package org.fishwife.jrugged;

import java.util.concurrent.Callable;


public class ConstantFlowThrottler implements ServiceWrapper {

    /**
     * By default a value of -1 allows all requests to go through
     * unmolested.
     */
    private int requestPerSecondThreshold = -1;

    /**
     *
     */
    private long deltaWaitTimeMillis = 0;

    /**
     *
     */
    private long lastRequestOccurance = 0;

    public ConstantFlowThrottler() {
    }

    public ConstantFlowThrottler(int requestsPerMinute) {
        setRequestPerSecondThreshold(requestsPerMinute);
    }

    public <T> T invoke(Callable<T> c) throws Exception {
        sleepIfCallRateExceeded();
        return c.call();
    }

    public void invoke(Runnable r) throws Exception {
        sleepIfCallRateExceeded();
        r.run();
    }

    public <T> T invoke(Runnable r, T result) throws Exception {
        sleepIfCallRateExceeded();
        r.run();
        return result;
    }

    protected synchronized void sleepIfCallRateExceeded() throws Exception {
        if (requestPerSecondThreshold == -1) {
            return;
        }

        if (lastRequestOccurance == 0) {
            lastRequestOccurance = System.currentTimeMillis();
            return;
        }

        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestOccurance;
        if (timeSinceLastRequest > deltaWaitTimeMillis) {
            lastRequestOccurance = System.currentTimeMillis();
            return;
        }

        lastRequestOccurance = System.currentTimeMillis();
        long timeToSleep = deltaWaitTimeMillis - timeSinceLastRequest;
        Thread.sleep(timeToSleep);
    }

    public void setRequestPerSecondThreshold(int i) {
        this.requestPerSecondThreshold = i;
        calculateDeltaWaitTime();
    }

    public int getRequestPerSecondThreshold() {
        return requestPerSecondThreshold;
    }

    private void calculateDeltaWaitTime() {
        if (requestPerSecondThreshold > 0) {
            deltaWaitTimeMillis = 1000L / requestPerSecondThreshold;
        }
    }

}
