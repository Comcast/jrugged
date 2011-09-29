/* TestHardwareClock.java
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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.fishwife.jrugged.interval.DiscreteInterval;


public class TestHardwareClock {

    private HardwareClock.Env mockEnv;
    private HardwareClock impl;
    private Random random;
    
    @Before
    public void setUp() {
        random = new Random();
        mockEnv = createMock(HardwareClock.Env.class);
        impl = new HardwareClock(mockEnv);
    }
    
    @Test
    public void normalizesToPositiveReadings() {
        expect(mockEnv.nanoTime()).andReturn(-100L);
        expect(mockEnv.nanoTime()).andReturn(-90L).anyTimes();
        expect(mockEnv.currentTimeMillis()).andReturn(0L).anyTimes();
        replay(mockEnv);
        DiscreteInterval out = impl.getNanoTime();
        verify(mockEnv);
        assertTrue(out.getMin() >= 0);
        assertTrue(out.getMax() >= 0);
    }
    
    @Test
    public void elapsedTimeWorksIfNoElapsedTime() {
       assertEquals(0L, impl.elapsedTime(1L, 1L)); 
    }
    
    @Test
    public void elapsedTimeWorksWhenNoOverflow() {
        assertEquals(3L, impl.elapsedTime(4L,7L));
    }
    
    @Test
    public void elapsedTimeWorksWhenOverflowed() {
        long start = Long.MAX_VALUE - 3L;
        long end = start + 10L;
        assertEquals(10L, impl.elapsedTime(start, end));
    }
    
    @Test
    public void canSampleImmediatelyIncrementingClock() {
        expect(mockEnv.nanoTime()).andReturn(4L);
        expect(mockEnv.nanoTime()).andReturn(5L);
        replay(mockEnv);
        assertEquals(1L, impl.sampleGranularity());
        verify(mockEnv);
    }
    
    @Test
    public void samplesClockUntilItTicks() {
        int i = random.nextInt(10) + 2;
        expect(mockEnv.nanoTime()).andReturn(4L).times(i);
        expect(mockEnv.nanoTime()).andReturn(10L);
        replay(mockEnv);
        assertEquals(6L, impl.sampleGranularity());
        verify(mockEnv);
    }
    
    @Test
    public void canRetrieveClockReadingInterval() {
        impl = new HardwareClock();
        assertTrue(impl.getNanoTime() instanceof DiscreteInterval);
    }
    
    @Test
    public void errorIsHalfOfGranularityForEvenGranularity() {
        impl = new HardwareClock(mockEnv) {
            public long getGranularity() { return 6L; }
        };
        expect(mockEnv.nanoTime()).andReturn(10L).anyTimes();
        replay(mockEnv);
        assertEquals(7L, impl.getNanoTime().size());
        verify(mockEnv);
    }
    
    @Test
    public void errorRoundsProperlyForOddGranularity() {
        impl = new HardwareClock(mockEnv) {
            public long getGranularity() { return 5L; }
        };
        expect(mockEnv.nanoTime()).andReturn(10L).anyTimes();
        replay(mockEnv);
        assertEquals(7L, impl.getNanoTime().size());
        verify(mockEnv);
    }
    
    @Test
    public void canTakeMultipleReadings() {
        impl = new HardwareClock();
        assertNotNull(impl.getNanoTime());
        assertNotNull(impl.getNanoTime());
    }
}
