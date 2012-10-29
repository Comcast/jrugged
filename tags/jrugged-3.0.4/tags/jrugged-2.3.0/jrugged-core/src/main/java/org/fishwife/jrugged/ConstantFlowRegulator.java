package org.fishwife.jrugged;

import java.util.concurrent.Callable;

public class ConstantFlowRegulator implements ServiceWrapper {

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

    public ConstantFlowRegulator() {
    }

    public ConstantFlowRegulator(int requestsPerSecond) {
        requestPerSecondThreshold = requestsPerSecond;
        calculateDeltaWaitTime();
    }

    public <T> T invoke(Callable<T> c) throws Exception {
        if (canProceed()) {
            return c.call();
        }
        else {
            throw new FlowRateExceededException();
        }
    }

    public void invoke(Runnable r) throws Exception {
        if (canProceed()) {
            r.run();
        }
        else {
            throw new FlowRateExceededException();
        }
    }

    public <T> T invoke(Runnable r, T result) throws Exception {
        if (canProceed()) {
            r.run();
            return result;
        }
        else {
            throw new FlowRateExceededException();
        }
    }

    protected synchronized boolean canProceed() {
        if (requestPerSecondThreshold == -1) {
            return true;
        }

        if (lastRequestOccurance == 0) {
            lastRequestOccurance = System.currentTimeMillis();
            return true;
        }

        if ((System.currentTimeMillis() - lastRequestOccurance) > deltaWaitTimeMillis) {
            lastRequestOccurance = System.currentTimeMillis();
            return true;
        }

        return false;
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
