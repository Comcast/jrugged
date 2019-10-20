/* RollingCounter.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

import java.util.LinkedList;

/**
 * Keeps a count of the number of events that have occurred within a given time
 * window.
 */
public class WindowedEventCounter {

	/**
	 * The {@link Clock} to used to determine current time (override for testing).
	 */
	private Clock clock = new SystemClock();

	/**
	 * Length of the window in milliseconds.
	 */
	private long windowMillis;

	/**
	 * Storage for the internal queue.
	 */
	private final LinkedList<Long> queue = new LinkedList<Long>();

	/**
	 * The maximum count this WindowedEventCounter can hold. Also, the maximum queue
	 * size. Immutable.
	 */
	private int capacity;

	/**
	 * Sole constructor.
	 *
	 * @param capacity     maximum count this WindowedEventCounter can hold.
	 * @param windowMillis length of the interest window in milliseconds.
	 * @throws IllegalArgumentException if capacity is less than 1 or if
	 *                                  windowMillis is less than 1.
	 */
	public WindowedEventCounter(int capacity, long windowMillis) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be greater than 0");
		}
		if (windowMillis <= 0) {
			throw new IllegalArgumentException("windowMillis must be greater than 0");
		}
		this.windowMillis = windowMillis;
		this.capacity = capacity;
	}

	/**
	 * Record a new event.
	 */
	public void mark() {
		final long currentTimeMillis = clock.currentTimeMillis();

		synchronized (queue) {
			if (queue.size() == capacity) {
				/*
				 * we're all filled up already, let's dequeue the oldest timestamp to make room
				 * for this new one.
				 */
				queue.removeFirst();
			}
			queue.addLast(currentTimeMillis);
		}
	}

	/**
	 * Returns a count of in-window events.
	 *
	 * @return the the count of in-window events.
	 */
	public int tally() {
		long currentTimeMillis = clock.currentTimeMillis();

		// calculates time for which we remove any errors before
		final long removeTimesBeforeMillis = currentTimeMillis - windowMillis;

		synchronized (queue) {
			// drain out any expired timestamps but don't drain past empty
			while (!queue.isEmpty() && queue.peek() < removeTimesBeforeMillis) {
				queue.removeFirst();
			}
			return queue.size();
		}
	}

	/**
	 * Returns the length of the currently configured event window in milliseconds.
	 *
	 * @return <code>long</code>
	 */
	public long getWindowMillis() {
		return windowMillis;
	}

	/**
	 * Specifies the maximum capacity of the counter.
	 *
	 * @param capacity <code>long</code>
	 * @throws IllegalArgumentException if windowMillis is less than 1.
	 */
	public void setCapacity(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be greater than 0");
		}

		synchronized (queue) {
			// If the capacity was reduced, we remove oldest elements until the
			// queue fits inside the specified capacity
			if (capacity < this.capacity) {
				while (queue.size() > capacity) {
					queue.removeFirst();
				}
			}
		}

		this.capacity = capacity;
	}

	/**
	 * Returns the maximum capacity this counter can hold.
	 *
	 * @return <code>int</code>
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Specifies the length of the interest window in milliseconds.
	 *
	 * @param windowMillis <code>long</code>
	 * @throws IllegalArgumentException if windowMillis is less than 1.
	 */
	public void setWindowMillis(long windowMillis) {
		if (windowMillis <= 0) {
			throw new IllegalArgumentException("windowMillis must be greater than 0");
		}

		// changing windowMillis while tally() is draining expired events could
		// lead to weirdness, let's lock for this.
		synchronized (queue) {
			this.windowMillis = windowMillis;
		}
	}

	/**
	 * Allow the internal {@link Clock} that is used for current time to be
	 * overridden (for testing).
	 *
	 * @param clock <code>Clock</code>
	 */
	protected void setClock(Clock clock) {
		this.clock = clock;
	}

}
