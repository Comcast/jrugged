/* FlowMeter.java
 * 
 * Copyright (C) 2009 Jonathan T. Moore
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

/** This class uses a raw {@link RequestCounter} to compute request rates
 *  over time for total rate of request, rate of successful requests, and
 *  rate of failed requests.
 */
public class FlowMeter {
    private RequestCounter counter;

    private long lastTotal;
    private long lastSuccesses;
    private long lastFailures;
    private long lastSampleMillis;

    /** Constructs a {@link FlowMeter}.
     *  @param counter the {@link RequestCounter} to calculate request
     *    rates from
     */
    public FlowMeter(RequestCounter counter) {
	this.counter = counter;
    }

    /** Calculates requests per second. */
    private double rate(long events, long t) {
	return ((double)events / (double)t) * 1000.0;
    }

    /** Takes a sample of the request rates. Calculations are based on
     *  differences in request counts since the last call to 
     *  <code>sample()</code>.
     *  @return an array of three <code>doubles</code>: total requests per
     *    second, successful requests per second, failed requests per
     *    second. If this is the first sample, all three rates will be
     *    reported as zero requests per second.
     */
    public synchronized double[] sample() {
	long[] currCounts = counter.sample();
	long now = System.currentTimeMillis();
	double[] out = new double[3];

	if (lastSampleMillis != 0) {
	    long deltaTime = now - lastSampleMillis;

	    out[0] = rate(currCounts[0] - lastTotal, deltaTime);
	    out[1] = rate(currCounts[1] - lastSuccesses, deltaTime);
	    out[2] = rate(currCounts[2] - lastFailures, deltaTime);

	} else {
	    out[0] = out[1] = out[2] = 0.0;
	}
	
	lastTotal = currCounts[0];
	lastSuccesses = currCounts[1];
	lastFailures = currCounts[2];
	lastSampleMillis = now;
	
	return out;
    }
}