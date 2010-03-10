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
package org.fishwife.jrugged;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Trips if the number of failures in a given time window exceed a
 * specified tolerance.  By default, all {@link Throwable} occurrences
 * will be considered failures.
 */
public final class DefaultFailureInterpreter implements FailureInterpreter {

    private Set<Class<? extends Throwable>> ignore = new HashSet<Class<? extends Throwable>>();
    private int limit = 0;
    private long windowMillis = 0;

    // tracks times when exceptions occurred
    private List<Long> errorTimes = Collections
            .synchronizedList(new LinkedList<Long>());

	private static Class[] defaultIgnore = { };

    /**
     * Default constructor. Any {@link Throwable} will cause the breaker to trip.
     */
    public DefaultFailureInterpreter() {
		setIgnore(defaultIgnore);
	}

    /**
     * Constructor that allows a tolerance for a certain number of
     * failures within a given window of time without tripping.
     * @param limit the number of failures that will be tolerated
     *   (i.e. the number of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the limit is 3, the fourth failure during
     *   the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
	public DefaultFailureInterpreter(int limit, long windowMillis) {
		setIgnore(defaultIgnore);
		setLimit(limit);
		setWindowMillis(windowMillis);
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
	public DefaultFailureInterpreter(Class<? extends Throwable>[] ignore) {
		setIgnore(ignore);
	}

    /**
     * Constructor where we specify tolerance and a set of ignored failures.
     *
     * @param ignore an array of {@link Throwable} classes that will
     *   be ignored. Any given <code>Throwable</code> that is a
     *   subclass of one of these classes will be ignored. 
     * @param limit the number of failures that will be tolerated
     *   (i.e. the number of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the limit is 3, the fourth failure during 
     *   the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
	public DefaultFailureInterpreter(Class<? extends Throwable>[] ignore,
									 int limit, long windowMillis) {
		setIgnore(ignore);
		setLimit(limit);
		setWindowMillis(windowMillis);
	}

    private boolean hasWindowConditions() {
        return this.limit > 0 && this.windowMillis > 0;
    }
	
	public boolean shouldTrip(Throwable cause) {
		for(Class clazz : ignore) {
			if (clazz.isInstance(cause)) {
				return false;
			}
		}

		// if Exception is of specified type, and window conditions exist,
		// keep circuit open unless exception threshold has passed
		if (hasWindowConditions()) {
			errorTimes.add(System.currentTimeMillis());

			// calculates time for which we remove any errors before
			final long removeTimesBeforeMillis = System.currentTimeMillis()
				- windowMillis;
			
			// removes errors before cutoff 
			// (could we speed this up by using binary search to find the entry point,
			// then removing any items before that point?)
			for (final Iterator<Long> i = this.errorTimes.iterator(); i.hasNext();) {
				final Long time = i.next();
				if (time < removeTimesBeforeMillis) {
					i.remove();
				} else {
					// the list is sorted by time, if I didn't remove this item
					// I won't be remove any after it either
					break;
				}
			}
			
			// Trip if the exception count has passed the limit
			return (errorTimes.size() > limit);
		} 
		return true;
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
    public int getLimit(){
        return this.limit;
    }
    /**
     * Specifies the number of tolerated failures within the configured time window.
     * @param limit <code>int</code>
     */
    public void setLimit(int limit) {
        this.limit=limit;
    }

    /**
     * Returns the length of the currently configured tolerance window.
     * @return <code>long</code>
     */
    public long getWindowMillis(){
        return this.windowMillis;
    }

    /**
     * Specifies the length of the tolerance window.
     * @param windowMillis <code>long</code>
     */
    public void setWindowMillis(long windowMillis) {
        this.windowMillis=windowMillis;
    }
}
