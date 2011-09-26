/* HardwareClock.java
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
package org.fishwife.jrugged.clocks;

import java.util.concurrent.atomic.AtomicLong;

/** This class captures the &quot;most accurate system timer&quot;
 * available via {@link System#nanoTime}, but also attempts to determine
 * the actual granularity of the timer (which might be greater than 1ns)
 * and present the measurement error inherent in taking clock measurements.
 */
class HardwareClock {

    private static long DEFAULT_PERIOD_MILLIS = 100 * 1000L;
    private long periodMillis = DEFAULT_PERIOD_MILLIS;
    private static int DEFAULT_NUM_SAMPLES = 100;
    
    private AtomicLong lastSampleTime = new AtomicLong(0L);
    private int sampleIndex = 0;
    private long maxGranularity;
    private long[] samples;
    private Env env;
    
    /** Default constructor. */
    public HardwareClock() {
        this(new DefaultEnv(), DEFAULT_NUM_SAMPLES, DEFAULT_PERIOD_MILLIS);
    }
    
    /** Constructs a new <code>HardwareClock</code> with an alternative
     * implementation for various static system methods. Primarily useful
     * for testing.
     * @param env
     */
    public HardwareClock(Env env) {
        this(env, DEFAULT_NUM_SAMPLES, DEFAULT_PERIOD_MILLIS);
    }
    
    /** Constructs a new <code>HardwareClock</code> with all dependencies
     * and/or configuration specified.
     * @param env alternative implementation of required static system methods
     * @param numSamples how many granularity samples to keep historically (default
     *   is 100)
     * @param periodMillis how often to sample the clock granularity, in milliseconds
     *   (default is 100,000, or once every 100 seconds)
     */
    public HardwareClock(Env env, int numSamples, long periodMillis) {
        this.env = env;
        samples = new long[numSamples];
        this.periodMillis = periodMillis;
    }
    
    long elapsedTime(long start, long end) {
        if (end > start) return (end - start);
        return (Long.MAX_VALUE - start) + 1 + (end - Long.MIN_VALUE);
    }
    
    long sampleGranularity() {
        long start = env.nanoTime();
        long end;
        while((end = env.nanoTime()) == start) /* loop */;
        return elapsedTime(start, end);
    }
    
    /** Gets the estimated hardware clock granularity. This is the
     * number of nanoseconds that elapse between ticks/updates of
     * the underlying hardware clock.
     * @return granularity in nanoseconds
     */
    public long getGranularity() {
        long now = env.currentTimeMillis();
        long curr = lastSampleTime.get();
        if (now - curr > periodMillis
                && lastSampleTime.compareAndSet(curr, now)) {
            samples[sampleIndex] = sampleGranularity();
            sampleIndex = (sampleIndex + 1) % samples.length;
            long max = 0L;
            for(long sample : samples) {
                if (sample > max) max = sample;
            }
            maxGranularity = max;
        }
        return maxGranularity;
    }
    
    /** Returns the current measurement error of the hardware clock,
     * measured in picoseconds. By definition, this is taken to be
     * half of the measured granularity.
     * @return measurement error in picoseconds
     */
    public long getMeasurementErrorPicoSeconds() {
        return getGranularity() * 1000L / 2;
    }

    /** Interface capturing various static methods that are dependencies
     * of this class; this allows us to switch them out for testing and
     * generally keep us loosely coupled from underlying platform API.
     */
    public static interface Env {
        /** @see {@link System#nanotime} */
        long nanoTime();
        /** @see {@link System#currentTimeMillis} */
        long currentTimeMillis();
    }
    
    /** The default implementation for static dependencies encapsulated
     *  in the <code>Env</code> interface.
     */
    public static class DefaultEnv implements Env {
        /** Returns <code>System.nanoTime()</code>. */
        public long nanoTime() {
            return System.nanoTime();
        }
        /** Returns <code>System.currentTimeMillis()</code>. */
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }
}
