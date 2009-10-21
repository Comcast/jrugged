/* CircuitBreaker.java
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

import java.util.concurrent.Callable;

/** A {@link CircuitBreaker} can be used with a service to throttle traffic
 *  to a failed subsystem (particularly one we might not be able to monitor,
 *  such as a peer system which must be accessed over the network). Service
 *  calls are wrapped by the <code>CircuitBreaker</code>.
 *  <p>
 *  When everything is operating normally, the <code>CircuitBreaker</code>
 *  is OPEN and the calls are allowed through.
 *  <p>
 *  When a call fails, however, the <code>CircuitBreaker</code> "trips" and
 *  moves to a CLOSED state. Client calls are not allowed through while
 *  the <code>CircuitBreaker</code> is CLOSED.
 *  <p>
 *  After a certain "cooldown" period, the <code>CircuitBreaker</code> will
 *  transition to a HALF_OPEN state, where one call is allowed to go through
 *  as a test. If that call succeeds, the <code>CircuitBreaker</code> moves
 *  back to the OPEN state; if it fails, it moves back to the CLOSED state
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
    
    /** Represents whether a {@link CircuitBreaker} is OPEN, HALF_OPEN,
     *  or CLOSED. */
    protected enum BreakerState {
	OPEN, HALF_OPEN, CLOSED;
    };
    protected BreakerState state = BreakerState.OPEN;
    protected long lastFailure;
    protected long resetMillis = 15 * 1000L;
    protected boolean isAttemptLive = false;
    protected FailureInterpreter failureInterpreter = null;
    private boolean isHardTrip;
    
    public CircuitBreaker() {}
    
    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws {@link CircuitBreakerException} if the breaker was CLOSED or
     *    HALF_OPEN and this attempt wasn't the reset attempt
     *  @throws {@link Exception} if c throws an Exception
     */
    public <V> V invoke(Callable<V> c) throws Exception {
	if (!allowRequest()) throw new CircuitBreakerException();
	try {
	    V result; 
	    if(this.failureInterpreter != null)
           result = failureInterpreter.invoke(c);
	    else
	        result = c.call();
	    open();
	    return result;
	} catch (CircuitShouldStayOpenException e) {
        // I don't want to close the circuit, just pass on the exception
        final Throwable cause = e.getCause();
        if (cause instanceof Exception)
            throw (Exception) cause;
        else if (cause instanceof Error)
            throw (Error) cause;
        else
            throw new RuntimeException(cause);
    } catch (Exception e) {
	    trip();
	    throw e;
	}
    }

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws {@link CircuitBreakerException} if the breaker was CLOSED or
     *    HALF_OPEN and this attempt wasn't the reset attempt
     *  @throws {@link Exception} if <code>r</code> throws an Exception
     */
    public void invoke(Runnable r) throws Exception {
	if (!allowRequest()) throw new CircuitBreakerException();
	try {
	    r.run();
	    open();
	} catch (Exception e) {
	    trip();
	    throw e;
	}
    }

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *  @return result
     *  @throws {@link CircuitBreakerException} if the breaker was CLOSED or
     *    HALF_OPEN and this attempt wasn't the reset attempt
     *  @throws {@link Exception} if <code>r</code> throws an Exception
     */
    public <V> V invoke(Runnable r, V result) throws Exception {
	if (!allowRequest()) throw new CircuitBreakerException();
	try {
	    r.run();
	    open();
	    return result;
	} catch (Exception e) {
	    trip();
	    throw e;
	}
    }

    /** Causes the {@link CircuitBreaker} to trip and CLOSE; no new
     *  requests will be allowed until the <code>CircuitBreaker</code>
     *  resets. */
    public void trip() {
	state = BreakerState.CLOSED;
	lastFailure = System.currentTimeMillis();
	isAttemptLive = false;
    }

    
    /**
     * Trips the CircuitBreaker until {@link #reset()} is invoked.
     */
    public void tripHard() {
        this.trip();
        isHardTrip = true;
    }
    
    /** 
     * Manually set the breaker to be reset and ready for use.  This
     * is only useful after a manual trip otherwise the breaker will
     * trip automatically again if the service is still unavailable.
     * Just like a real breaker.  WOOT!!!
     */
    public void reset() {
        state = BreakerState.OPEN;
        isHardTrip = false;
    }

    /** Reports a successful service call to the {@link CircuitBreaker},
     *  putting the <code>CircuitBreaker</code> back into the OPEN
     *  state serving requests. */
    private void open() {
	state = BreakerState.OPEN;
	isAttemptLive = false;
    }

    private synchronized boolean canAttempt() {
	if (!BreakerState.HALF_OPEN.equals(state) 
	    || isAttemptLive) return false;
	isAttemptLive = true;
	return true;
    }

    /** Allows the client service to ask if it should attempt a service
     *  call. */
    private boolean allowRequest() {
	if (this.isHardTrip) return false;
	else if (BreakerState.OPEN.equals(state)) return true;
	
	if (BreakerState.CLOSED.equals(state) && 
	    System.currentTimeMillis() - lastFailure >= resetMillis) {
	    state = BreakerState.HALF_OPEN;
	}
	return canAttempt();
    }

    /** Returns the current {@link Status} of the {@link CircuitBreaker}.
     *  In this case, it really refers to the status of the client service.
     *  If the <code>CircuitBreaker</code> is OPEN, we report that the
     *  client is UP; if it is HALF_OPEN or CLOSED, we report the client
     *  is DOWN. */
    public Status getStatus() {
	return (BreakerState.OPEN.equals(state) 
		? Status.UP 
		: Status.DOWN);
    }
    
    public long getResetMillis() {
        return resetMillis;
    }

    /** Sets the reset period to the given number of milliseconds; the
     *  default is 15,000 (make one retry attempt every 15 seconds). */
    public CircuitBreaker setResetMillis(long l) { resetMillis = l; return this; }

    public CircuitBreaker setFailureInterpreter(FailureInterpreter failureInterpreter) {
        this.failureInterpreter = failureInterpreter;
        return this;
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
    
}
