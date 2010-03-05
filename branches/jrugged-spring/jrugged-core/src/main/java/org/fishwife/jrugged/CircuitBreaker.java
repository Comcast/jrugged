/* CircuitBreaker.java
 * 
 * Copyright 2009 Comcast Interactive Media, LLC.
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
        OPEN, HALF_CLOSED, CLOSED
    }

    protected BreakerState state = BreakerState.CLOSED;
    protected long lastFailure;
    protected long openCount = 0L;
    protected long resetMillis = 15 * 1000L;
    protected boolean isAttemptLive = false;

    protected FailureInterpreter failureInterpreter = new DefaultFailureInterpreter();
    protected CircuitBreakerExceptionMapper exceptionMapper;

    private boolean isHardTrip;
    
    public CircuitBreaker() {}

	public CircuitBreaker(FailureInterpreter fi) {
		failureInterpreter = fi;
	}
    
    public CircuitBreaker(CircuitBreakerExceptionMapper mapper) {
        exceptionMapper = mapper;
    }

    public CircuitBreaker(FailureInterpreter fi, CircuitBreakerExceptionMapper mapper) {
        failureInterpreter = fi;
        exceptionMapper = mapper;
    }

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws Exception {@link CircuitBreakerException} if the breaker was OPEN or
     *    HALF_CLOSED and this attempt wasn't the reset attempt
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

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws Exception {@link CircuitBreakerException} if the breaker was OPEN or
     *    HALF_CLOSED and this attempt wasn't the reset attempt
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

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *  @return result
     *  @throws Exception {@link CircuitBreakerException} if the breaker was OPEN or
     *    HALF_CLOSED and this attempt wasn't the reset attempt
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
     * Allow people to ask the system when the last circuit open event was
     *
     * @return long the last failure time
     */
    public long getLastTripTime() {
        return lastFailure;
    }

    /**
     * Allow people to ask the system what the number of
     * times that the breaker tripped open was.
     *
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

    /** Returns the current {@link org.fishwife.jrugged.Status} of the {@link CircuitBreaker}.
     *  In this case, it really refers to the status of the client service.
     *  If the <code>CircuitBreaker</code> is CLOSED, we report that the
     *  client is UP; if it is HALF_CLOSED, we report that the client is
     *  DEGRADED; if it is OPEN, we report the client is DOWN.
     */
    public Status getStatus() {
        if (state == BreakerState.OPEN
                && !isHardTrip
                && lastFailure > 0 
                && (System.currentTimeMillis() - lastFailure >= resetMillis)) {

            return Status.DEGRADED;
        }

        switch(state) {
            case OPEN: return Status.DOWN;
            case HALF_CLOSED: return Status.DEGRADED;
            case CLOSED: return Status.UP;
            default: return (!isHardTrip && lastFailure > 0 && (System.currentTimeMillis() - lastFailure >= resetMillis) ? Status.DEGRADED: Status.UP);
        }
    }

    /**
     * Return the cooldown period in milliseconds.
     * @return long
     */
    public long getResetMillis() {
        return resetMillis;
    }

    /** Sets the reset period to the given number of milliseconds; the
     *  default is 15,000 (make one retry attempt every 15 seconds).
     *
     * @param l long in milliseconds for when I should reset this circuit.
     */
    public void setResetMillis(long l) {
        resetMillis = l;
    }

    /**
     * Specifies a helper that determines whether a given failure will cause the breaker to trip or not.
     *
     * @param failureInterpreter the interp
     */
    public void setFailureInterpreter(FailureInterpreter failureInterpreter) {
        this.failureInterpreter = failureInterpreter;
    }

    /**
     * Get the failure interpreter for this instance.  The failure interpreter
     * provides the configuration for determining which exceptions trip
     * the circuit breaker, in what time interval, etc.
     *
     * @return The FailureInterpreter for this instance or null if no failure
     * interpreter was set..
     */
    public FailureInterpreter getFailureInterpreter(){
        return this.failureInterpreter;
    }

    /**
     * A helper that converts CircuitBreakerExceptions into a known 'application' exception.
     *
     * @param mapper my converter object
     */
    public void setExceptionMapper(CircuitBreakerExceptionMapper mapper) {
        this.exceptionMapper = mapper;

    }

    /**
     * get the helper that converts CircuitBreakerExceptions into a known 'application' exception.
     *
     * @return CircuitBreakerExceptionMapper my converter object, or <code>null</code> if one is
     * not currently set.
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
     * putting the <code>CircuitBreaker</code> back into the Closed
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
     * Allows the client service to ask if it should attempt a service
     * call.
     *
     * @return boolean '
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
