/* TestSampledQuantile.java
 *
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class TestSampledQuantile {

    private SampledQuantile impl;

    @Before
    public void setUp() {
        impl = new SampledQuantile();
    }

    @Test
    public void quantileWithNoSamplesShouldReturnZero() {
        assertEquals(0, impl.getPercentile(50));
    }

    @Test
    public void quantileWithOneSampleShouldReturnThatSample() {
        impl.addSample(42);
        assertEquals(42, impl.getPercentile(50));
    }

    @Test
    public void medianOfThreeSamplesIsMiddleSample() {
        impl.addSample(42);
        impl.addSample(41);
        impl.addSample(43);
        assertEquals(42, impl.getPercentile(50));
    }

    @Test
    public void medianOfFiveSamplesWithRepeatsStillWorks() {
        impl.addSample(41);
        impl.addSample(43);
        impl.addSample(42);
        impl.addSample(41);
        impl.addSample(43);
        assertEquals(42, impl.getPercentile(50));
    }

    @Test
    public void medianOfTwoSamplesIsTheirAverage() {
        impl.addSample(41);
        impl.addSample(43);
        assertEquals(42, impl.getPercentile(50));
    }

    @Test
    public void canGetMedianAsExpressedInQuantiles() {
        impl.addSample(42);
        impl.addSample(41);
        impl.addSample(43);
        assertEquals(42, impl.getQuantile(1,2));
    }

    @Test
    public void canGetMedianDirectly() {
        impl.addSample(42);
        impl.addSample(41);
        impl.addSample(43);
        assertEquals(42, impl.getMedian());
    }

    @Test
    public void zerothQuantileShouldThrowException() {
        impl.addSample(41);
        try {
            impl.getQuantile(0,7);
            fail("should have thrown exception");
        } catch (SampledQuantile.QuantileOutOfBoundsException expected) {
        }
    }

    @Test
    public void qthQuantileShouldThrowException() {
        impl.addSample(41);
        try {
            impl.getQuantile(7,7);
            fail("should have thrown exception");
        } catch (SampledQuantile.QuantileOutOfBoundsException expected) {
        }
    }

    @Test
    public void canSpecifyMaxSamples() {
        impl = new SampledQuantile(10);
        for(int i=0; i<20; i++) impl.addSample(0);
        assertEquals(10, impl.getNumSamples());
    }

    @Test
    public void canSpecifyCurrentTimeWhenAddingSample() {
        impl.addSample(41, System.currentTimeMillis());
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedSecondWindow() {
        impl = new SampledQuantile(60, TimeUnit.SECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 90 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedNanosecondWindow() {
        impl = new SampledQuantile(60 * 1000000000L, TimeUnit.NANOSECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 90 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedMicrosecondWindow() {
        impl = new SampledQuantile(60 * 1000000L, TimeUnit.MICROSECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 90 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedMillisecondWindow() {
        impl = new SampledQuantile(60 * 1000L, TimeUnit.MILLISECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 90 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedMinuteWindow() {
        impl = new SampledQuantile(60L, TimeUnit.SECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 90 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedHourWindow() {
        impl = new SampledQuantile(3600L, TimeUnit.SECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 5400 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void ignoresSamplesOutsideOfSpecifiedDayWindow() {
        impl = new SampledQuantile(86400L, TimeUnit.SECONDS);
        long now = System.currentTimeMillis();
        impl.addSample(7, now - 2 * 24 * 3600 * 1000L);
        impl.addSample(42, now);
        assertEquals(42, impl.getPercentile(50, now+1));
    }

    @Test
    public void windowedSamplingWorks() {
        long t0 = System.currentTimeMillis();
        impl = new SampledQuantile(10, 60L, TimeUnit.SECONDS, t0);
        for(int t=0; t<30 * 1000; t++) {
            impl.addSample(1L, t0 + t);
        }
        long t1 = t0 + 30 * 1000L;
        assertEquals(1L, impl.getPercentile(50, t1));

        for(int t=0; t<60*1000; t++) {
            impl.addSample(2L, t1 + t);
        }
        long t2 = t1 + 60 * 1000L;
        assertEquals(2L, impl.getPercentile(50, t2));
        impl.addSample(3L, t2+1);
    }

    @Test
    public void windowedSamplingHandlesLongTimesBetweenSamples() {
        long t0 = System.currentTimeMillis();
        impl = new SampledQuantile(10, 60L, TimeUnit.SECONDS, t0);
        impl.addSample(1L, t0 + 1);
        long t1 = t0 + 90 * 1000L;
        impl.addSample(2L, t1);
        assertEquals(2L, impl.getPercentile(50, t1));
    }
}
