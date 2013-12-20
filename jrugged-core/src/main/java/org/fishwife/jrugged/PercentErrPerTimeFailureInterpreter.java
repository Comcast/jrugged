/* Copyright 2009-2012 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Trips a {@link org.fishwife.jrugged.CircuitBreaker} if the percentage of failures in a given
 * time window exceed a specified tolerance.  By default, all {@link
 * Throwable} occurrences will be considered failures.
 */
public final class PercentErrPerTimeFailureInterpreter implements FailureInterpreter {

    private Set<Class<? extends Throwable>> ignore = new HashSet<Class<? extends Throwable>>();
    private int percent = 0;
    private long windowMillis = 0;
    private int requestThreshold = 0;
    private long previousRequestHighWaterMark = 0;

    // tracks times when exceptions occurred
    private List<Long> errorTimes = new LinkedList<Long>();

    // tracks times when exceptions occurred
    private List<Long> requestCounts = new LinkedList<Long>();
    private final Object modificationLock = new Object();

    private RequestCounter requestCounter;

	private static Class[] defaultIgnore = { };

    /**
     * Default constructor. Any {@link Throwable} will cause the breaker to trip.
     */
    @SuppressWarnings("unchecked")
    public PercentErrPerTimeFailureInterpreter() {
		setIgnore(defaultIgnore);
        requestCounter = new RequestCounter();
	}

    /**
     * Constructor that allows a tolerance for a certain number of
     * failures within a given window of time without tripping.
     * @param rc A {@link RequestCounter} wrapped around the same thing that this
     *   {@link org.fishwife.jrugged.CircuitBreaker} is protecting.  This is
     *   needed in order to keep track of the total number of requests, enabling a
     *   percentage calculation to be done.
     * @param percent the whole number percentage of failures that will be tolerated
     *   (i.e. the percentage of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the percentage is 3, any calculated failure percentage
     *   above that number during the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
    @SuppressWarnings("unchecked")
	public PercentErrPerTimeFailureInterpreter(RequestCounter rc,
                                               int percent, long windowMillis) {
		setIgnore(defaultIgnore);
        setPercent(percent);
		setWindowMillis(windowMillis);
        setRequestCounter(rc);
	}

    /**
     * Constructor that allows a tolerance for a certain number of
     * failures within a given window of time without tripping.
     * @param p A {@link PerformanceMonitor} from which we can get an underlying
     *   {@link RequestCounter} that is wrapped around the same thing that this
     *   {@link org.fishwife.jrugged.CircuitBreaker} is protecting.  This is
     *   needed in order to keep track of the total number of requests, enabling a
     *   percentage calculation to be done.
     * @param percent the whole number percentage of failures that will be tolerated
     *   (i.e. the percentage of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the percentage is 3, any calculated failure percentage
     *   above that number during the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
    @SuppressWarnings("unchecked")
	public PercentErrPerTimeFailureInterpreter(PerformanceMonitor p,
                                               int percent, long windowMillis) {
		setIgnore(defaultIgnore);
        setPercent(percent);
		setWindowMillis(windowMillis);
        setRequestCounter(p.getRequestCounter());
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
	public PercentErrPerTimeFailureInterpreter(Class<? extends Throwable>[] ignore) {
		setIgnore(ignore);
	}

    /**
     * Constructor where we specify tolerance and a set of ignored failures.
     *
     * @param rc A {@link RequestCounter} wrapped around the same thing that this
     *   {@link org.fishwife.jrugged.CircuitBreaker} is protecting.  This is
     *   needed in order to keep track of the total number of requests, enabling a
     *   percentage calculation to be done.
     * @param ignore an array of {@link Throwable} classes that will
     *   be ignored. Any given <code>Throwable</code> that is a
     *   subclass of one of these classes will be ignored.
     * @param percent the whole number percentage of failures that will be tolerated
     *   (i.e. the percentage of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the percentage is 3, any calculated failure percentage
     *   above that number during the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
	public PercentErrPerTimeFailureInterpreter(RequestCounter rc,
                                               Class<? extends Throwable>[] ignore,
									           int percent, long windowMillis) {
        setRequestCounter(rc);
		setIgnore(ignore);
		setPercent(percent);
		setWindowMillis(windowMillis);
	}

    private boolean hasWindowConditions() {
        return this.percent > 0 && this.windowMillis > 0;
    }

	public boolean shouldTrip(Throwable cause) {
        if (isExceptionIgnorable(cause)) return false;

        // if Exception is of specified type, and window conditions exist,
        // keep circuit open unless exception threshold has passed
        if (hasWindowConditions()) {
            Long currentRequestCount = -1L;
            long numberOfErrorsAfter;

            synchronized (modificationLock) {
                errorTimes.add(System.currentTimeMillis());
                requestCounts.add(requestCounter.sample()[0]);

                // calculates time for which we remove any errors before
                final long removeTimeBeforeMillis = System.currentTimeMillis() - windowMillis;
                final int numberOfErrorsBefore = this.errorTimes.size();

                removeErrorsPriorToCutoffTime(numberOfErrorsBefore, removeTimeBeforeMillis);

                numberOfErrorsAfter = this.errorTimes.size();

                currentRequestCount = this.requestCounts.get(requestCounts.size() - 1);
            }

            long windowRequests = (currentRequestCount - previousRequestHighWaterMark);

            // Trip if the number of errors over the total of requests over the same period
            // is over the percentage limit.
            return windowRequests >= requestThreshold && (((double) numberOfErrorsAfter / (double) windowRequests) * 100d >= percent);
        }
        return true;
	}

    private boolean isExceptionIgnorable(Throwable cause) {
        for(Class clazz : ignore) {
            if (clazz.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }

    private void removeErrorsPriorToCutoffTime(int numberOfErrorsBefore, long removeTimeBeforeMillis) {
        boolean windowRemoval = false;
        
        // (could we speed this up by using binary search to find the entry point,
        // then removing any items before that point?)
        for (int j = numberOfErrorsBefore - 1; j >= 0; j--) {

            final Long time = this.errorTimes.get(j);

            if (time < removeTimeBeforeMillis) {
                if (!windowRemoval) {
                    previousRequestHighWaterMark = requestCounts.get(j);
                    windowRemoval = true;
                }

                this.errorTimes.remove(j);
                this.requestCounts.remove(j);
            }
        }
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
     * Returns the current percentage of failures within the window that will be tolerated
     * without tripping the breaker.
     * @return int
     */
    public int getPercent(){
        return this.percent;
    }

    /**
     * Specifies the percentage of tolerated failures within the
     * configured time window. If percentage is set to <em>n</em> then the
     * <em>(n.000000000000001)</em>th failure will trip the breaker.
     * @param percent <code>int</code>
     */
    public void setPercent(int percent) {
        this.percent=percent;
    }

    /**
     * Returns the length of the currently configured tolerance window
     * in milliseconds.
     * @return <code>long</code>
     */
    public long getWindowMillis(){
        return this.windowMillis;
    }

    /**
     * Specifies the length of the tolerance window in milliseconds.
     * @param windowMillis <code>long</code>
     */
    public void setWindowMillis(long windowMillis) {
        this.windowMillis=windowMillis;
    }

    /**
     * Specifies the {@link RequestCounter} that will be supplying the "total" requests
     * made information for this interpreter.
     * @param rc A {@link RequestCounter}
     */
    public void setRequestCounter(RequestCounter rc) {
        requestCounter = rc;
    }

    /**
     * Sets the threshold at which the number of requests in the current window must be above in order for the breaker
     * to trip. This is intended to prevent the breaker from being opened if the first request into this interpreter is
     * a failure.
     *
     * @param requestThreshold The threshold to set, defaults to 0
     */
    public void setRequestThreshold(int requestThreshold) {
        this.requestThreshold = requestThreshold;
    }
}
