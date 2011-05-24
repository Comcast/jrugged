/* TestRollingCounter.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public final class TestWindowedEventCounter {

	private WindowedEventCounter impl;

	private StoppedClock clock = new StoppedClock();

	private static int CAPACITY = 3;

	private static long WINDOW_MILLIS = 5L;

	@Before
	public void setUp() {
		impl = new WindowedEventCounter(CAPACITY, WINDOW_MILLIS);
		clock.currentTimeMillis = System.currentTimeMillis();
		impl.setClock(clock);
		
		// several tests depend on this assumption
		assertTrue(WINDOW_MILLIS > CAPACITY);
	}

	// constructor tests
	@Test
	public void testConstructor() {
		assertEquals(CAPACITY, impl.getCapacity());
		assertEquals(WINDOW_MILLIS, impl.getWindowMillis());
	}

	@Test
	public void testConstructorThrowsExceptionOnBadCapacity() {
		try {
			@SuppressWarnings("unused")
            WindowedEventCounter brokenCounter = new WindowedEventCounter(0, WINDOW_MILLIS);
			fail("constructer should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// this is expected. ignore and let test pass.
		}
	}

	@Test
	public void testConstructorThrowsExceptionOnBadWindowMillis() {
		try {
			@SuppressWarnings("unused")
            WindowedEventCounter brokenCounter = new WindowedEventCounter(CAPACITY, 0);
			fail("constructer should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// this is expected. ignore and let test pass.
		}
	}

	@Test
	public void testStartsEmpty() {
		assertEquals(0, impl.tally());
	}

	@Test
	public void testCountsToCapacity() {
		for (int i = 1; i <= CAPACITY; i++) {
			impl.mark();
			clock.currentTimeMillis = clock.currentTimeMillis + 1;
			assertEquals(i, impl.tally());
		}
	}

	@Test
	public void testCountsToCapacityOnOverflow() {
		for (int i = 0; i < (CAPACITY * 2); i++) {
			clock.currentTimeMillis = clock.currentTimeMillis + 1;
			impl.mark();
		}
		assertEquals(CAPACITY, impl.tally());
	}

	@Test
	public void testRollingExpiration() {
		// empty at t0
		long t0 = clock.currentTimeMillis;

		/*
		 * fill 'er up, marking once per milli (first event is at t1, second
		 * event at t2...)
		 */
		for (int i = 1; i <= CAPACITY; i++) {
			clock.currentTimeMillis = t0 + i;
			impl.mark();
		}

		/*
		 * represents that last time that all the events should still be
		 * in-window.
		 */
		clock.currentTimeMillis = t0 + 1 + WINDOW_MILLIS;
		int expectedCount = CAPACITY;
		assertEquals(CAPACITY, impl.tally());

		// the tally count should drain at a rate one per milli now
		for (int j = 1; j <= CAPACITY; j++) {
			clock.currentTimeMillis++;
			expectedCount--;
			assertEquals(expectedCount, impl.tally());
		}

		clock.currentTimeMillis++;
		// we should be empty now
		assertEquals(0, impl.tally());
	}

    @Test
    public void testReducingWindowDecreasesTally() throws Exception {
		// empty at t0
		long t0 = clock.currentTimeMillis;

		/*
		 * fill 'er up, marking once per milli (first event is at t1, second
		 * event at t2...)
		 */
		for (int i = 1; i <= CAPACITY; i++) {
			clock.currentTimeMillis = t0 + i;
			impl.mark();
		}

        // Move time to 1 MS past the last entry.
        clock.currentTimeMillis = clock.currentTimeMillis + 1;

        impl.setWindowMillis(1);
        assertEquals(1, impl.tally());
    }

	@Test
	public void testReducingCapacityDecreasesTally() {
		// fill up to capacity
		for (int i = 1; i <= CAPACITY; i++) {
			impl.mark();
		}
		assertEquals(CAPACITY, impl.tally());

		// reduce capacity
		impl.setCapacity(CAPACITY - 1);
		assertEquals(CAPACITY - 1, impl.tally());
	}

	@Test
	public void testIncreasingCapacity() {
		// fill up to capacity
		for (int i = 1; i <= CAPACITY; i++) {
			impl.mark();
		}
		assertEquals(CAPACITY, impl.tally());

		impl.setCapacity(CAPACITY + 1);

		assertEquals(CAPACITY, impl.tally());
	}

	@Test
	public void testSettingBadCapacityThrowsException() {
		try {
			impl.setCapacity(0);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// this is expected. ignore and let test pass.
		}
	}

	@Test
	public void testSettingBadWindowMillisThrowsException() {
		try {
			impl.setWindowMillis(0);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// this is expected. ignore and let test pass.
		}
	}

	public class StoppedClock implements Clock {
		public long currentTimeMillis;

		public long currentTimeMillis() {
			return currentTimeMillis;
		}
	}

}