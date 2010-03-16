/* CircuitBreaker.java
 * 
 * Copyright 2009-2010 Comcast Interactive Media, LLC.
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

import java.util.Collection;
import java.util.concurrent.Callable;

/** A {@link CircuitBreaker} can be used with a service to throttle traffic
 *  to a failed subsystem (particularly one we might not be able to monitor,
 *  such as a peer system which must be accessed over the network). Service
 *  calls are wrapped by the <code>CircuitBreaker</code>.
 *  <p>
 *  When everything is operating normally, the <code>CircuitBreaker</code>
 *  is CLOSED and the calls are allowed through.
 *  <p>
 *  When a call fails, however, the <code>CircuitBreaker</code> "trips" and
 *  moves to an OPEN state. Client calls are not allowed through while
 *  the <code>CircuitBreaker</code> is OPEN.
 *  <p>
 *  After a certain "cooldown" period, the <code>CircuitBreaker</code> will
 *  transition to a HALF_CLOSED state, where one call is allowed to go through
 *  as a test. If that call succeeds, the <code>CircuitBreaker</code> moves
 *  back to the CLOSED state; if it fails, it moves back to the OPEN state
 *  for another cooldown period.
 *  <p>
 *  Sample usage:
 *  <pre>
    public class Service implements Monitorable {
        private CircuitBreaker cb = new CircuitBreaker();
        public String doSomething(final Object arg) throws Exception {
	    return cb.invoke(new Callable&lt;String>() {
                                 public String call() {
                                     // make the call ...
                                 }
                             });
        }
        public Status getStatus() { return cb.getStatus(); }
    }
 * </pre>
 */
public class CircuitBreaker implements Monitorable, ServiceWrapper {

    /**
     * Represents whether a {@link CircuitBreaker} is OPEN, HALF_CLOSED,
     *  or CLOSED.
     */
    protected enum BreakerState {
		/** An OPEN breaker has tripped and will not allow requests
			through. */
        OPEN, 
			
		/** A HALF_CLOSED breaker has completed its cooldown
			period and will allow one request through as a "test request." */
		HALF_CLOSED, 

		/** A CLOSED breaker is operating normally and allowing
			requests through. */
		CLOSED
    }

	/** Current state of the breaker. */
    protected BreakerState state = BreakerState.CLOSED;

	/** The time the breaker last tripped, in milliseconds since the
		epoch. */
    protected long lastFailure;

	/** How many times the breaker has tripped during its lifetime. */
    protected long openCount = 0L;
	
	/** How long the cooldown period is in milliseconds. */
    protected long resetMillis = 15 * 1000L;

	/** Whether the "test" attempt permitted in the HALF_CLOSED state
	 *  is currently in-flight. */
    protected boolean isAttemptLive = false;

	/** The {@link FailureInterpreter} to use to determine whether a
		given failure should cause the breaker to trip. */
    protected FailureInterpreter failureInterpreter = 
		new DefaultFailureInterpreter();

	/** Helper class to allow throwing an application-specific
	 * exception rather than the default {@link
	 * CircuitBreakerException}. */
    protected CircuitBreakerExceptionMapper exceptionMapper;

    private boolean isHardTrip;

	/** Creates a {@link CircuitBreaker} with a {@link
	 *  DefaultFailureInterpreter} and the default "tripped" exception
	 *  behavior (throwing a {@link CircuitBreakerException}). */
    public CircuitBreaker() {}

	/** Creates a {@link CircuitBreaker} with the specified {@link
	 *	FailureInterpreter} and the default "tripped" exception
	 *	behavior (throwing a {@link CircuitBreakerException}).
	 *  @param fi the <code>FailureInterpreter</code> to use when
	 *    determining whether a specific failure ought to cause the 
	 *    breaker to trip
	 */
	public CircuitBreaker(FailureInterpreter fi) {
		failureInterpreter = fi;
	}

	/** Creates a {@link CircuitBreaker} with a {@link
	 *  DefaultFailureInterpreter} and using the supplied {@link
	 *  CircuitBreakerExceptionMapper} when client calls are made
	 *  while the breaker is tripped.
	 *  @param mapper helper used to translate a {@link
	 *    CircuitBreakerException} into an application-specific one */
    public CircuitBreaker(CircuitBreakerExceptionMapper mapper) {
        exceptionMapper = mapper;
    }

	/** Creates a {@link CircuitBreaker} with the provided {@link
	 *  FailureInterpreter} and using the provided {@link
	 *  CircuitBreakerExceptionMapper} when client calls are made
	 *  while the breaker is tripped.
	 *  @param fi the <code>FailureInterpreter</code> to use when
	 *    determining whether a specific failure ought to cause the 
	 *    breaker to trip
	 *  @param mapper helper used to translate a {@link
	 *    CircuitBreakerException} into an application-specific one */
    public CircuitBreaker(FailureInterpreter fi, 
						  CircuitBreakerExceptionMapper mapper) {
        failureInterpreter = fi;
        exceptionMapper = mapper;
    }

    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws CircuitBreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
    public <V> V invoke(Callable<V> c) throws Exception {
        if (!allowRequest()) {
            throw mappedException(new CircuitBreakerException());
        }

		try {
            V result = c.call();
            close();
			return result;
		} catch (Throwable cause) {
			handleFailure(cause);
		}
		throw new IllegalStateException("not possible");
    }

    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws CircuitBreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
    public void invoke(Runnable r) throws Exception {
        if (!allowRequest()) {
            throw mappedException(new CircuitBreakerException());
        }

        try {
    	    r.run();
	        close();
        } catch (Throwable cause) {
			handleFailure(cause);
        }
		throw new IllegalStateException("not possible");
    }

    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *  @return result
     *  @throws CircuitBreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
	 *  @throws Exception if <code>c</code> throws one during
	 *    execution
     */
    public <V> V invoke(Runnable r, V result) throws Exception {
    	if (!allowRequest()) {
            throw mappedException(new CircuitBreakerException());
        }

        try {
            r.run();
            close();
            return result;
        } catch (Throwable cause) {
			handleFailure(cause);
        }
		throw new IllegalStateException("not possible");
    }

    /**
     * Causes the {@link CircuitBreaker} to trip and OPEN; no new
     *  requests will be allowed until the <code>CircuitBreaker</code>
     *  resets.
     */
    public void trip() {
        state = BreakerState.OPEN;
        lastFailure = System.currentTimeMillis();
        openCount++;
        isAttemptLive = false;
    }
    
    /**
     * Manually trips the CircuitBreaker until {@link #reset()} is invoked.
     */
    public void tripHard() {
        this.trip();
        isHardTrip = true;
    }

    /**
     * Returns the last time the breaker tripped OPEN, measured in
     * milliseconds since the Epoch.
     * @return long the last failure time
     */
    public long getLastTripTime() {
        return lastFailure;
    }

    /**
	 * Returns the number of times the breaker has tripped OPEN during
	 * its lifetime.
     * @return long the number of times the circuit breaker tripped
     */
    public long getTripCount() {
        return openCount;
    }

    /**
     * Manually set the breaker to be reset and ready for use.  This
     * is only useful after a manual trip otherwise the breaker will
     * trip automatically again if the service is still unavailable.
     * Just like a real breaker.  WOOT!!!
     */
    public void reset() {
        state = BreakerState.CLOSED;
        isHardTrip = false;
    }

    /** Returns the current {@link org.fishwife.jrugged.Status} of the
     *  {@link CircuitBreaker}.  In this case, it really refers to the
     *  status of the client service.  If the
     *  <code>CircuitBreaker</code> is CLOSED, we report that the
     *  client is UP; if it is HALF_CLOSED, we report that the client
     *  is DEGRADED; if it is OPEN, we report the client is DOWN.
     */
    public Status getStatus() {
		boolean canSendProbeRequest = !isHardTrip && lastFailure > 0
			&& (System.currentTimeMillis() - lastFailure >= resetMillis);

        switch(state) {
		case OPEN: 
			return (canSendProbeRequest ? Status.DEGRADED : Status.DOWN);
		case HALF_CLOSED: return Status.DEGRADED;
		case CLOSED: 
		default:
			return Status.UP;
        }
    }

    /**
     * Returns the cooldown period in milliseconds.
     * @return long
     */
    public long getResetMillis() {
        return resetMillis;
    }

    /** Sets the reset period to the given number of milliseconds. The
     *  default is 15,000 (make one retry attempt every 15 seconds).
     *
     * @param l number of milliseconds to "cool down" after tripping
     *   before allowing a "test request" through again
     */
    public void setResetMillis(long l) {
        resetMillis = l;
    }

	/** Returns a {@link String} representation of the breaker's
	 * status; potentially useful for exposing to monitoring software.
	 * @return <code>String</code> which is <code>"GREEN"</code> if
	 *   the breaker is CLOSED; <code>"YELLOW"</code> if the breaker
	 *   is HALF_CLOSED; and <code>"RED"</code> if the breaker is
	 *   OPEN (tripped). */
    public String getHealthCheck() { return getStatus().getSignal(); }

    /**
     * Specifies the failure tolerance limit for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param limit the number of tolerated failures in a window
     */
    public void setLimit(int limit) {
		FailureInterpreter fi = getFailureInterpreter();
		if (!(fi instanceof DefaultFailureInterpreter)) {
			throw new IllegalStateException("setLimit() not supported: this CircuitBreaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
		}
        ((DefaultFailureInterpreter)fi).setLimit(limit);
    }

	/**
     * Specifies a set of {@link Throwable} classes that should not
	 *  be considered failures by the {@link CircuitBreaker}.
	 *  @see DefaultFailureInterpreter
	 *  @param ignore a {@link java.util.Collection} of {@link Throwable}
	 *  classes
	 */
	public void setIgnore(Collection<Class<? extends Throwable>> ignore) {
		FailureInterpreter fi = getFailureInterpreter();
		if (!(fi instanceof DefaultFailureInterpreter)) {
			throw new IllegalStateException("setIgnore() not supported: this CircuitBreaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
		}
		
		Class[] classes = new Class[ignore.size()];
		int i = 0;
		for(Class c : ignore) {
			classes[i] = c;
			i++;
		}
		((DefaultFailureInterpreter)fi).setIgnore(classes);
	}

    /**
     * Specifies the tolerance window in milliseconds for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param windowMillis length of the window in milliseconds
     */
    public void setWindowMillis(long windowMillis) {
		FailureInterpreter fi = getFailureInterpreter();
		if (!(fi instanceof DefaultFailureInterpreter)) {
			throw new IllegalStateException("setWindowMillis() not supported: this CircuitBreaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
		}
		((DefaultFailureInterpreter)fi).setWindowMillis(windowMillis);
    }

    /**
     * Specifies a helper that determines whether a given failure will
     * cause the breaker to trip or not.
     *
     * @param failureInterpreter the {@link FailureInterpreter} to use
     */
    public void setFailureInterpreter(FailureInterpreter failureInterpreter) {
        this.failureInterpreter = failureInterpreter;
    }

    /**
     * Get the failure interpreter for this instance.  The failure
     * interpreter provides the configuration for determining which
     * exceptions trip the circuit breaker, in what time interval,
     * etc.
     *
     * @return {@link FailureInterpreter} for this instance or null if no
     * failure interpreter was set.
     */
    public FailureInterpreter getFailureInterpreter() {
        return this.failureInterpreter;
    }

    /**
     * A helper that converts CircuitBreakerExceptions into a known
     * 'application' exception.
     *
     * @param mapper my converter object
     */
    public void setExceptionMapper(CircuitBreakerExceptionMapper mapper) {
        this.exceptionMapper = mapper;
    }

    /**
     * Get the helper that converts {@link CircuitBreakerException}s into
	 * application-specific exceptions.
     * @return {@link CircuitBreakerExceptionMapper} my converter object, or
     *   <code>null</code> if one is not currently set.
     */
    public CircuitBreakerExceptionMapper getExceptionMapper(){
        return this.exceptionMapper;
    }

    private Exception mappedException(CircuitBreakerException cbe) {
        if (exceptionMapper == null) return cbe;
        return exceptionMapper.map(this, cbe);
    }

	private void handleFailure(Throwable cause) throws Exception {
		if (failureInterpreter == null ||
			failureInterpreter.shouldTrip(cause)) {
			trip();
		}
		if (cause instanceof Exception) {
			throw (Exception)cause;
		} else if (cause instanceof Error) {
			throw (Error)cause;
		} else {
			throw (RuntimeException)cause;
		}
	}

    /**
     * Reports a successful service call to the {@link CircuitBreaker},
     * putting the <code>CircuitBreaker</code> back into the CLOSED
     * state serving requests.
     */
    private void close() {
        state = BreakerState.CLOSED;
        isAttemptLive = false;
    }

    private synchronized boolean canAttempt() {
        if (!BreakerState.HALF_CLOSED.equals(state) || isAttemptLive) {
            return false;
        }
        isAttemptLive = true;
        return true;
    }

    /**
     * @return boolean whether the breaker will allow a request
     * through or not.
     */
    private boolean allowRequest() {
        if (this.isHardTrip) {
            return false;
        }
        else if (BreakerState.CLOSED.equals(state)) {
            return true;
        }

        if (BreakerState.OPEN.equals(state) &&
            System.currentTimeMillis() - lastFailure >= resetMillis) {
            state = BreakerState.HALF_CLOSED;
        }
        return canAttempt();
    }
}
