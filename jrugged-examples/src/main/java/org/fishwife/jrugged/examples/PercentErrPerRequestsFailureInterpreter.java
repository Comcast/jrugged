/* Copyright 2009-2010 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.examples;

import org.fishwife.jrugged.FailureInterpreter;
import org.fishwife.jrugged.PerformanceMonitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PercentErrPerRequestsFailureInterpreter implements FailureInterpreter {
    private Set<Class<? extends Throwable>> ignore = new HashSet<Class<? extends Throwable>>();
    protected PerformanceMonitor pMonitor;

    protected AtomicInteger percent = new AtomicInteger();
    protected AtomicLong requestTolerance = new AtomicLong();

    private AtomicLong requestSnapshot = new AtomicLong();
    private AtomicLong errorSnapshot = new AtomicLong();

    public PercentErrPerRequestsFailureInterpreter(PerformanceMonitor pm) {
        pMonitor = pm;
    }

    /**
     * Constructor where we specify certain {@link Throwable} classes
     * that will be ignored by the breaker and not be treated as
     * failures (they will be passed through transparently without
     * causing the breaker to trip).
     * @param ignore an array of {@link Throwable} classes that will
     *   be ignored. Any given <code>Throwable</code> that is a
     *   subclass of one of these classes will be ignored.
     */
	public PercentErrPerRequestsFailureInterpreter(Class<? extends Throwable>[] ignore) {
		setIgnore(ignore);
	}

    /**
     * Constructor where we specify tolerance and a set of ignored failures.
     *
     * @param ignore an array of {@link Throwable} classes that will
     *   be ignored. Any given <code>Throwable</code> that is a
     *   subclass of one of these classes will be ignored.
     * @param percent the number of failures that will be tolerated
     *   (i.e. the number of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the limit is 3, the fourth failure during
     *   the window will cause the breaker to trip.
     * @param requestCount length of the window in milliseconds
     */
	public PercentErrPerRequestsFailureInterpreter(Class<? extends Throwable>[] ignore,
									 int percent, long requestCount) {
		setIgnore(ignore);
		setPercent(percent);
		setRequestTolerance(requestCount);
	}

    public boolean shouldTrip(Throwable cause) {
        for(Class clazz : ignore) {
            if (clazz.isInstance(cause)) {
                return false;
            }
        }

        if (requestSnapshot.get() == 0) {
            requestSnapshot.set(pMonitor.getRequestCount());
            errorSnapshot.set(pMonitor.getFailureCount());
        }

        // If at least the number of requests indicated by my tolerance
        // have occurred, check to see if I should trip.
        if (requestSnapshot.get() + requestTolerance.get() < pMonitor.getRequestCount()) {
            if (((pMonitor.getFailureCount() - errorSnapshot.get()) / requestTolerance.get()) * 100 > percent.get()) {
                requestSnapshot.set(pMonitor.getRequestCount());
                errorSnapshot.set(pMonitor.getFailureCount());
                return true;
            }
            requestSnapshot.set(pMonitor.getRequestCount());
            errorSnapshot.set(pMonitor.getFailureCount());
        }
        return false;
    }

    /**
     * Returns the set of currently ignored {@link Throwable} classes.
     * @return {@link Set}
     */
    public Set<Class<? extends Throwable>> getIgnore(){
        return this.ignore;
    }

    /**
     * Specifies an array of {@link Throwable} classes to ignore. These will not
     * be considered failures.
     * @param ignore array of {@link Class} objects
     */
    public synchronized void setIgnore(Class<? extends Throwable>[] ignore) {
		this.ignore = new HashSet<Class<? extends Throwable>>(Arrays.asList(ignore));
    }

    /**
     * Returns the current number of failures within the window that will be tolerated
     * without tripping the breaker.
     * @return int
     */
    public int getPercent(){
        return this.percent.get();
    }

    /**
     * Specifies the number of tolerated failures within the
     * configured time window. If limit is set to <em>n</em> then the
     * <em>(n+1)</em>th failure will trip the breaker.
     * @param percent <code>int</code>
     */
    public void setPercent(int percent) {
        this.percent.set(percent);
    }

    /**
     * Returns the current number of failures within the window that will be tolerated
     * without tripping the breaker.
     * @return long
     */
    public long getRequestTolerance(){
        return this.requestTolerance.get();
    }

    /**
     * Specifies the number of tolerated failures within the
     * configured time window. If limit is set to <em>n</em> then the
     * <em>(n+1)</em>th failure will trip the breaker.
     * @param requestTolerance <code>long</code>
     */
    public void setRequestTolerance(long requestTolerance) {
        this.requestTolerance.set(requestTolerance);
    }
}
