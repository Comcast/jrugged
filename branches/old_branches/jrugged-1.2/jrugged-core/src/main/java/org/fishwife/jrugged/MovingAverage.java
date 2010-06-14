/* MovingAverage.java
 * 
 * Copyright 2009 Comcast Interactive Media, LLC.
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

/** This class implements an exponential moving average, using the
 *  algorithm described at <a href="http://en.wikipedia.org/wiki/Moving_average">http://en.wikipedia.org/wiki/Moving_average</a>. The average does not
 *  sample itself; it merely computes the new average when updated with
 *  a sample by an external polling mechanism. */
public class MovingAverage {
    private long windowMillis;
    private long lastMillis;
    private double average;

    /** Construct a {@link MovingAverage}, providing the time window
     *  we want the average over.
     *  @param windowMillis the length of the sliding window in
     *    milliseconds */
    public MovingAverage(long windowMillis) {
	this.windowMillis = windowMillis;
    }

    /** Updates the average with the latest measurement.
     *  @param sample the latest measurement in the rolling average */
    public synchronized void update(double sample) {
	long now = System.currentTimeMillis();
	if (lastMillis == 0) {	// first sample
	    average = sample;
	    lastMillis = now;
	    return;
	}
	long deltaTime = now - lastMillis;
	double coeff = Math.exp(-1.0 * ((double)deltaTime / windowMillis));
	average = (1.0 - coeff) * sample + coeff * average;
	lastMillis = now;
    }

    /** Returns the last computed average value. */
    public double getAverage() { return average; }
}