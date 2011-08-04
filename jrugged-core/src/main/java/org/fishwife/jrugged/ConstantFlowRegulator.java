/* ConstantFlowRegulator.java
 * 
 * Copyright 2009-2011 Comcast Interactive Media, LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     *  Wrap the given service call with the {@link ConstantFlowRegulator}
     *  protection logic.
     *  @param c the {@link Callable} to attempt
     *
     *  @return whatever c would return on success
     *
     *  @throws FlowRateExceededException if the total requests per second
     *    through the flow regulator exceeds the configured value
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
    public <T> T invoke(Callable<T> c) throws Exception {
        if (canProceed()) {
            return c.call();
        }
        else {
            throw new FlowRateExceededException();
        }
    }

    /** Wrap the given service call with the {@link ConstantFlowRegulator}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *
     *  @throws FlowRateExceededException if the total requests per second
     *    through the flow regulator exceeds the configured value
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
    public void invoke(Runnable r) throws Exception {
        if (canProceed()) {
            r.run();
        }
        else {
            throw new FlowRateExceededException();
        }
    }

    /** Wrap the given service call with the {@link ConstantFlowRegulator}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *
     *  @return result
     *
     *  @throws FlowRateExceededException if the total requests per second
     *    through the flow regulator exceeds the configured value
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
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

    /**
     * Configures number of requests per second to allow through this flow regulator
     * onto a configured back end service.
     *
     * @param i the requests per second threshold for this flow regulator
     */
    public void setRequestPerSecondThreshold(int i) {
        this.requestPerSecondThreshold = i;
        calculateDeltaWaitTime();
    }

    /**
     * Returns the currently configured number of requests per second to allow
     * through this flow regulator onto a configured back end service.
     *
     * @return int the currently configured requests per second threshold
     */
    public int getRequestPerSecondThreshold() {
        return requestPerSecondThreshold;
    }

    private void calculateDeltaWaitTime() {
        if (requestPerSecondThreshold > 0) {
            deltaWaitTimeMillis = 1000L / requestPerSecondThreshold;
        }
    }

}
