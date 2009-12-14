/* MovingAverageFactory.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Provides a convenient way to create {@link MovingAverage} objects that
 *  get updated on a regular period. */
public class MovingAverageFactory implements Runnable {
    private ScheduledFuture handle;
    private Map<MovingAverage, Callable<Double>> averages = 
	new HashMap<MovingAverage, Callable<Double>>();
    private ScheduledExecutorService sched = 
	Executors.newScheduledThreadPool(1);

    /** Sole constructor.
     *  @param updateFrequencySecs how often, in seconds, we want to update
     *    each {@link MovingAverage} created by this {@link MovingAverageFactory} */
    public MovingAverageFactory(int updateFrequencySecs) {
	handle = sched.scheduleAtFixedRate(this, 0, updateFrequencySecs,
					   TimeUnit.SECONDS);
    }

    /** Shuts down the recurring updates of moving averages as part of an
     *  orderly termination. */
    public void destroy() {
	handle.cancel(true);
	sched.shutdown();
    }

    /** Updates the moving averages. */
    public void run() {
	for(MovingAverage avg : averages.keySet()) {
	    try {
		avg.update(averages.get(avg).call());
	    } catch (Exception e) {
	    }
	}
    }

    /** Creates a new {@link MovingAverage}.
     *  @param windowMillis is the time window, in milliseconds, over
     *    which the <code>MovingAverage</code> will be calculated
     *  @param sample a {@link Callable} which will be called to take
     *    a sample measurement to feed the moving average. Sample 
     *    functions that throw exceptions will be silently ignored.
     *  @return the <code>MovingAverage</code> object */
    public MovingAverage makeMovingAverage(long windowMillis,
					   Callable<Double> sample) {
	MovingAverage out = new MovingAverage(windowMillis);
	averages.put(out, sample);
	return out;
    }
}