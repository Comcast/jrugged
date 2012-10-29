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
package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerExceptionMapper;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/** This is basically a {@link CircuitBreaker} that adds JMX
 * annotations to some of the methods so that the core library
 * doesn't have to depend on spring-context.
 */
@ManagedResource
public class CircuitBreakerBean extends CircuitBreaker {

	/** Creates a {@link CircuitBreakerBean} with a {@link
	 *  DefaultFailureInterpreter} and the default "tripped" exception
	 *  behavior (throwing a {@link CircuitBreakerException}). */
    public CircuitBreakerBean() { super(); }

	/** Creates a {@link CircuitBreakerBean} with the specified {@link
	 *	FailureInterpreter} and the default "tripped" exception
	 *	behavior (throwing a {@link CircuitBreakerException}).
	 *  @param fi the <code>FailureInterpreter</code> to use when
	 *    determining whether a specific failure ought to cause the 
	 *    breaker to trip
	 */
    public CircuitBreakerBean(FailureInterpreter fi) {
        super(fi);
    }

	/** Creates a {@link CircuitBreaker} with a {@link
	 *  DefaultFailureInterpreter} and using the supplied {@link
	 *  CircuitBreakerExceptionMapper} when client calls are made
	 *  while the breaker is tripped.
	 *  @param mapper helper used to translate a {@link
	 *    CircuitBreakerException} into an application-specific one */
    public CircuitBreakerBean(CircuitBreakerExceptionMapper mapper) {
        super(mapper);
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
    public CircuitBreakerBean(FailureInterpreter fi, CircuitBreakerExceptionMapper mapper) {
        super(fi, mapper);
    }

    /**
     * Manually trips the CircuitBreaker until {@link #reset()} is invoked.
     */
    @ManagedOperation
    @Override
    public void tripHard() {
        super.tripHard();
    }

    /**
     * Returns the last time the breaker tripped OPEN, measured in
     * milliseconds since the Epoch.
     * @return long the last failure time
     */
    @ManagedAttribute
    @Override
    public long getLastTripTime() {
        return super.getLastTripTime();
    }

    /**
	 * Returns the number of times the breaker has tripped OPEN during
	 * its lifetime.
     * @return long the number of times the circuit breaker tripped
     */
    @ManagedAttribute
    @Override
    public long getTripCount() {
        return super.getTripCount();
    }

    /**
     * Manually set the breaker to be reset and ready for use.  This
     * is only useful after a manual trip otherwise the breaker will
     * trip automatically again if the service is still unavailable.
     * Just like a real breaker.  WOOT!!!
     */
    @ManagedOperation
    @Override
    public void reset() {
        super.reset();
    }

    /**
     * Returns the cooldown period in milliseconds.
     * @return long
     */
    @ManagedAttribute
    @Override
    public long getResetMillis() {
        return super.getResetMillis();
    }

    /** Sets the reset period to the given number of milliseconds. The
     *  default is 15,000 (make one retry attempt every 15 seconds).
     *
     * @param l number of milliseconds to "cool down" after tripping
     *   before allowing a "test request" through again
     */
    @ManagedOperation
    @Override
    public void setResetMillis(long l) {
        super.setResetMillis(l);
    }

	/** Returns a {@link String} representation of the breaker's
	 * status; potentially useful for exposing to monitoring software.
	 * @return <code>String</code> which is <code>"GREEN"</code> if
	 *   the breaker is CLOSED; <code>"YELLOW"</code> if the breaker
	 *   is HALF_CLOSED; and <code>"RED"</code> if the breaker is
	 *   OPEN (tripped). */
    @ManagedAttribute
    @Override
	public String getHealthCheck() { return super.getHealthCheck(); }

    /**
     * Specifies the failure tolerance limit for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param limit the number of tolerated failures in a window
     */
    @ManagedOperation
    @Override
    public void setLimit(int limit) {
        ((DefaultFailureInterpreter) super.getFailureInterpreter()).setLimit(limit);
    }

    /**
     * Specifies the tolerance window in milliseconds for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param windowMillis length of the window in milliseconds
     */
    @ManagedOperation
    @Override
    public void setWindowMillis(long windowMillis) {
        super.setWindowMillis(windowMillis);
    }
}
