/* DefaultFailureInterpreter.java
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Trips a {@link CircuitBreaker} if the number of failures in a given
 * time window exceed a specified tolerance.  By default, all {@link
 * Throwable} occurrences will be considered failures.
 */
public final class DefaultFailureInterpreter implements FailureInterpreter {

    private Set<Class<? extends Throwable>> ignore = new HashSet<Class<? extends Throwable>>();
    private Set<Class<? extends Throwable>> target = new HashSet<Class<? extends Throwable>>();
    private int limit = 0;
    private long windowMillis = 0;

	private WindowedEventCounter counter;
	
	@SuppressWarnings("unchecked")
	private static Class<? extends Throwable>[] defaultIgnore = 
		new Class[0];
	
	@SuppressWarnings("unchecked")
    private static Class<? extends Throwable>[] defaultTarget = 
        new Class[0];

    /**
     * Default constructor. Any {@link Throwable} will cause the breaker to trip.
     */
    public DefaultFailureInterpreter() {
        this(defaultIgnore, defaultTarget, 0, 0);
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
	    this(defaultIgnore, defaultTarget, limit, windowMillis);
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
		this(ignore, defaultTarget, 0, 0);
	}

    /**
     * Constructor where we specify tolerance and a set of ignored failures.
     *
     * @param ignore an array of {@link Throwable} classes that will
     *   be ignored. Any given <code>Throwable</code> that is a
     *   subclass of one of these classes will be ignored. 
     * @param target an array of {@link Throwable} classes that will
     *   be considered as failures. Any given <code>Throwable</code>
     *   that is a subclass of one of these classes will be considered
     *   as failures.
     * @param limit the number of failures that will be tolerated
     *   (i.e. the number of failures has to be strictly <em>greater
     *   than</em> this number in order to trip the breaker). For
     *   example, if the limit is 3, the fourth failure during 
     *   the window will cause the breaker to trip.
     * @param windowMillis length of the window in milliseconds
     */
	public DefaultFailureInterpreter(Class<? extends Throwable>[] ignore,
	                                 Class<? extends Throwable>[] target,
									 int limit, long windowMillis) {
		setIgnore(ignore);
		setTarget(target);
		setLimit(limit);
		setWindowMillis(windowMillis);
		initCounter();
	}

    private boolean hasWindowConditions() {
        return this.limit > 0 && this.windowMillis > 0;
    }
	
    /**
     * {@inheritDoc}
     * <p>
     * Failure filtering rules:
     * <ul>
     * <li>If both 'ignore' and 'target' fields are empty, all throwables will
     * be considered failures.</li>
     * <li>If only 'ignore' field is set, all throwables except ones specified
     * in 'ignore' field will be considered failures.</li>
     * <li>If only 'target' field is set, all throwables specified in 'target'
     * field will be considered failures.</li>
     * </ul>
     */
	public boolean shouldTrip(Throwable cause) {
	    if (isIgnoreEmpty() && isTargetEmpty()) {
	        return shouldTripImpl(cause);
	    }
	    else if (!isIgnoreEmpty()) {
	        for(Class<?> clazz : ignore) {
	            if (clazz.isInstance(cause)) {
	                return false;
	            }
	        }
	        
	        return shouldTripImpl(cause);
	    }
	    else {
	        for(Class<?> clazz : target) {
                if (clazz.isInstance(cause)) {
                    return shouldTripImpl(cause);
                }
            }
	        
	        return false;
	    }
	}
	
	/**
	 * The actual logic that determines circuit breaker trip.
	 * 
	 * @param oops the {@link Throwable} failure that occurred
     * @return boolean <code>true</code> iff the circuit should trip
	 */
	private boolean shouldTripImpl(Throwable cause) {
	    
	    // if window conditions exist, keep circuit open unless exception
	    // threshold has passed
        if (hasWindowConditions()) {
            counter.mark();
            // Trip if the exception count has passed the limit
            return (counter.tally() > limit);
        }
        
        return true;
	}
	
	private void initCounter() {
		if (hasWindowConditions()) {
			int capacity = limit + 1;
			if (counter == null) {
				this.counter = new WindowedEventCounter(capacity,windowMillis);
			} else {
				if (capacity != counter.getCapacity()) {
					counter.setCapacity(capacity);
				}

				if (windowMillis != counter.getWindowMillis()) {
					counter.setWindowMillis(windowMillis);
				}
			}
		} else {
			// we're not under windowConditions, no counter needed
			counter = null;
		}
	}
	
	/**
	 * Verifies {@code ignore} and {@code target} fields.
	 * 
	 * @throws IllegalArgumentException if both {@code ignore} and
	 *   {@code target} fields are set. Only one field should be set.
	 */
	private void verifyIgnoreAndTarget() {
        if (!isIgnoreEmpty() && !isTargetEmpty()) {
	        throw new IllegalArgumentException("Cannot set both 'ignore' and 'target' fields. You can only use one or the other.");
	    }
	}
	
	private boolean isIgnoreEmpty() {
	    return (this.ignore == null) || (this.ignore.size() == 0);
	}
	
	private boolean isTargetEmpty() {
        return (this.target == null) || (this.target.size() == 0);
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
		verifyIgnoreAndTarget();
    }
    
    /**
     * Returns the set of currently targeted {@link Throwable} classes.
     * @return {@link Set}
     */
    public Set<Class<? extends Throwable>> getTarget(){
        return this.target;
    }

    /**
     * Specifies an array of {@link Throwable} classes to consider as failures.
     * @param target array of {@link Class} objects
     */
    public synchronized void setTarget(Class<? extends Throwable>[] target) {
        this.target = new HashSet<Class<? extends Throwable>>(Arrays.asList(target));
        verifyIgnoreAndTarget();
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
     * Specifies the number of tolerated failures within the
     * configured time window. If limit is set to <em>n</em> then the
     * <em>(n+1)</em>th failure will trip the breaker.  Mutating the
     * limit at runtime can reset previous failure counts.
     * @param limit <code>int</code>
     */
    public void setLimit(int limit) {
        this.limit=limit;
        initCounter();
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
        initCounter();
    }
}
