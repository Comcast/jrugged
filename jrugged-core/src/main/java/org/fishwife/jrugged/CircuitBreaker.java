/* CircuitBreaker.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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
import java.util.concurrent.atomic.AtomicLong;

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
        return cb.invoke(new Callable&lt;String&gt;() {
                                 public String call() {
                                     // make the call ...
                                 }
                             });
        }
        public Status getStatus() { return cb.getStatus(); }
    }
 * </pre>
 */
public class CircuitBreaker extends Breaker implements MonitoredService, ServiceWrapper {
	
	
	/** How long the cooldown period is in milliseconds. */
    protected AtomicLong resetMillis = new AtomicLong(15 * 1000L); 
   
    /**
     * Whether the "test" attempt permitted in the HALF_CLOSED state
     *  is currently in-flight.
     */
    protected boolean isAttemptLive = false;

    /** The default name if none is provided. */
    private static final String DEFAULT_NAME="CircuitBreaker";

    /** The name for the CircuitBreaker. */
    protected String name = DEFAULT_NAME;
    
    /** Creates a {@link CircuitBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}). */
    public CircuitBreaker() {
    }
    
    /** Creates a {@link CircuitBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link CircuitBreaker}.
     */
    public CircuitBreaker(String name) {
        super(name);
    }
    
    /** Creates a {@link CircuitBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public CircuitBreaker(FailureInterpreter fi) {
        super(fi);
    }

    /** Creates a {@link CircuitBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link CircuitBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public CircuitBreaker(String name, FailureInterpreter fi) {
        super(name, fi);
    }

    /** Creates a {@link CircuitBreaker} with a {@link
     *  DefaultFailureInterpreter} and using the supplied {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link CircuitBreaker}.
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public CircuitBreaker(String name, BreakerExceptionMapper<? extends Exception> mapper) {
        super(name, mapper);
    }

    /** Creates a {@link CircuitBreaker} with the provided {@link
     *  FailureInterpreter} and using the provided {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link CircuitBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public CircuitBreaker(String name,
                          FailureInterpreter fi,
                          BreakerExceptionMapper<? extends Exception> mapper) {
        super(name, fi, mapper);
    }

    
    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public <V> V invoke(Callable<V> c) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
                throw mapException(new BreakerException());
            }

            try {
                isAttemptLive = true;
                V result = c.call();
                close();
                return result;
            } catch (Throwable cause) {
                handleFailure(cause);
            }
            throw new IllegalStateException("not possible");
        }
        else {
            return c.call();
        }
    }

    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public void invoke(Runnable r) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
                throw mapException(new BreakerException());
            }

            try {
                isAttemptLive = true;
                r.run();
                close();
                return;
            } catch (Throwable cause) {
                handleFailure(cause);
            }
            throw new IllegalStateException("not possible");
        }
        else {
            r.run();
        }
    }

    /** Wrap the given service call with the {@link CircuitBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *  @return result
     *  @throws BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public <V> V invoke(Runnable r, V result) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
                throw mapException(new BreakerException());
            }

            try {
                isAttemptLive = true;
                r.run();
                close();
                return result;
            } catch (Throwable cause) {
                handleFailure(cause);
            }
            throw new IllegalStateException("not possible");
        }
        else {
            r.run();
            return result;
        }
    }

    /**
     * Causes the {@link CircuitBreaker} to trip and OPEN; no new
     *  requests will be allowed until the <code>CircuitBreaker</code>
     *  resets.
     */
    public void trip() {
        if (state != BreakerState.OPEN) {
            openCount.getAndIncrement();
        }
        state = BreakerState.OPEN;
        lastFailure.set(System.currentTimeMillis());
        isAttemptLive = false;

        notifyBreakerStateChange(getStatus());
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
        byPass = false;
        isAttemptLive = false;

        notifyBreakerStateChange(getStatus());
    }

    /**
     * Get the current {@link ServiceStatus} of the
     * {@link CircuitBreaker}, including the name,
     * {@link org.fishwife.jrugged.Status}, and reason.
     * @return the {@link ServiceStatus}.
     */
    public ServiceStatus getServiceStatus() {
        boolean canSendProbeRequest = !isHardTrip && lastFailure.get() > 0
            && allowRequest();

        if (byPass) {
            return new ServiceStatus(name, Status.DEGRADED, "Bypassed");
        }

        switch(state) {
            case OPEN:
                return (canSendProbeRequest ?
                        new ServiceStatus(name, Status.DEGRADED, "Send Probe Request")
                        : new ServiceStatus(name, Status.DOWN, "Open"));
            case HALF_CLOSED: return new ServiceStatus(name, Status.DEGRADED, "Half Closed");
            case CLOSED:
            default:
                return new ServiceStatus(name, Status.UP);
        }
    }

    /**
     * Returns the cooldown period in milliseconds.
     * @return long
     */
    public long getResetMillis() {
        return resetMillis.get();
    }

    /** Sets the reset period to the given number of milliseconds. The
     *  default is 15,000 (make one retry attempt every 15 seconds).
     *
     * @param l number of milliseconds to "cool down" after tripping
     *   before allowing a "test request" through again
     */
    public void setResetMillis(long l) {
        resetMillis.set(l);
    }

    protected void handleFailure(Throwable cause) throws Exception {
        if (failureInterpreter == null || failureInterpreter.shouldTrip(cause)) {
            this.tripException = cause;
            trip();
        }
        else if (isAttemptLive) {
            close();
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
    protected void close() {
        state = BreakerState.CLOSED;
        isAttemptLive = false;
        notifyBreakerStateChange(getStatus());
    }

    private synchronized boolean canAttempt() {
        if (!(BreakerState.HALF_CLOSED == state) || isAttemptLive) {
            return false;
        }
        return true;
    }

    /**
     * @return boolean whether the breaker will allow a request
     * through or not.
     */
    protected boolean allowRequest() {
        if (this.isHardTrip) {
            return false;
        }
        else if (BreakerState.CLOSED == state) {
            return true;
        }

        if (BreakerState.OPEN == state &&
            System.currentTimeMillis() - lastFailure.get() >= resetMillis.get()) {
            state = BreakerState.HALF_CLOSED;
        }

        return canAttempt();

    }

}
