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
 *  such as a peer system which must be accessed over the network). The
 *  client service asks the <code>CircuitBreaker</code> before initiating
 *  a request whether the request should be allowed. While the service is 
 *  operating normally, the <code>CircuitBreaker</code> stays open and all
 *  requests are allowed. 
 *  <p>
 *  However, the client may determine after a certain 
 *  number or type of errors have occurred that the remote system is down 
 *  or overloaded; the client service may then <code>trip()</code> the 
 *  <code>CircuitBreaker</code>. After tripping, the CircuitBreaker is
 *  CLOSED, and all new requests are denied for a certain "cooloff" or
 *  reset period, giving the failed system time to recover. 
 *  <p>
 *  Once the reset period has elapsed, the CircuitBreaker moves to a 
 *  HALF_OPEN state, where it allows one request through to test for 
 *  recovery. If the request fails, the client should <code>trip()</code> 
 *  the CircuitBreaker again back to CLOSED. If it succeeds, however,
 *  the client can <code>open()</code> the CircuitBreaker again to 
 *  restore normal operation.
 *  <p>
 *  Sample usage:
 *  <pre>
    public class Service implements Monitorable {
        private CircuitBreaker&lt;String> cb = new CircuitBreaker&lt;String>();
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
public class CircuitBreaker<V> implements Monitorable {
    
    /** Represents whether a {@link CircuitBreaker} is OPEN, HALF_OPEN,
     *  or CLOSED. */
    protected enum BreakerState {
	OPEN, HALF_OPEN, CLOSED;
    };
    protected BreakerState state = BreakerState.OPEN;
    protected long lastFailure;
    protected long resetMillis = 15 * 1000L;
    protected boolean isAttemptLive = false;

    /** Wrap the given service call with the CircuitBreaker protection
     *  logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws CircuitBreakerException if the breaker was CLOSED or
     *    HALF_OPEN and this attempt wasn't the reset attempt
     *  @throws Exception if c throws an Exception
     */
    public V invoke(Callable<V> c) throws Exception {
	if (!allowRequest()) throw new CircuitBreakerException();
	try {
	    V result = c.call();
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
    private void trip() {
	state = BreakerState.CLOSED;
	lastFailure = System.currentTimeMillis();
	isAttemptLive = false;
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
	if (BreakerState.OPEN.equals(state)) return true;
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

    /** Sets the reset period to the given number of milliseconds; the
     *  default is 15,000 (make one retry attempt every 15 seconds). */
    public void setResetMillis(long l) { resetMillis = l; }
}