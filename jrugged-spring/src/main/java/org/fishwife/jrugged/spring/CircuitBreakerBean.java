/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
import org.fishwife.jrugged.BreakerExceptionMapper;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is basically a {@link CircuitBreaker} that adds JMX
 * annotations to some of the methods so that the core library
 * doesn't have to depend on spring-context.
 */
@ManagedResource
public class CircuitBreakerBean extends CircuitBreaker implements InitializingBean {

    private boolean disabledAtStart = false;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (disabledAtStart) tripHard();
    }

    /**
     * Creates a {@link CircuitBreakerBean} with a
     * {@link DefaultFailureInterpreter} and the default "tripped" exception
     * behavior (throwing a {@link org.fishwife.jrugged.BreakerException}).
     */
    public CircuitBreakerBean() { super(); }

    /**
     * Creates a {@link CircuitBreakerBean} with a
     * {@link DefaultFailureInterpreter} and the default "tripped" exception
     * behavior (throwing a {@link org.fishwife.jrugged.BreakerException}).
     *  @param name the name for the {@link CircuitBreakerBean}
     */
    public CircuitBreakerBean(String name) { super(name); }

    /**
     * Creates a {@link CircuitBreakerBean} with the specified
     * {@link FailureInterpreter} and the default "tripped" exception
     * behavior (throwing a {@link org.fishwife.jrugged.BreakerException}).
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public CircuitBreakerBean(FailureInterpreter fi) {
      super(fi);
    }

    /**
     * Creates a {@link CircuitBreakerBean} with the specified
     * {@link FailureInterpreter} and the default "tripped" exception
     * behavior (throwing a {@link org.fishwife.jrugged.BreakerException}).
     *  @param name the name for the {@link CircuitBreakerBean}
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public CircuitBreakerBean(String name, FailureInterpreter fi) {
      super(name, fi);
    }

    /**
     * Creates a {@link CircuitBreaker} with a {@link
     *  DefaultFailureInterpreter} and using the supplied {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link CircuitBreakerBean}
     *  @param mapper helper used to translate a {@link
     *    org.fishwife.jrugged.BreakerException} into an application-specific one
     */
    public CircuitBreakerBean(String name, BreakerExceptionMapper<? extends Exception> mapper) {
      super(name, mapper);
    }

    /**
     * Creates a {@link CircuitBreaker} with the provided {@link
     *  FailureInterpreter} and using the provided {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *
     *  @param name the name for the {@link CircuitBreakerBean}
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     *  @param mapper helper used to translate a {@link
     *    org.fishwife.jrugged.BreakerException} into an application-specific one
     */
    public CircuitBreakerBean(String name, FailureInterpreter fi,
            BreakerExceptionMapper<? extends Exception> mapper) {
        super(name, fi, mapper);
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
     * Manually trips the CircuitBreaker until {@link #reset()} is invoked.
     */
    @ManagedOperation
    @Override
    public void trip() {
        super.trip();
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
     * When called with true - causes the {@link CircuitBreaker} to byPass
     * its functionality allowing requests to be executed unmolested
     * until the <code>CircuitBreaker</code> is reset or the byPass
     * is manually set to false.
     */
    @ManagedAttribute
    @Override
    public void setByPassState(boolean b) {
        super.setByPassState(b);
    }

    /**
     * Get the current state of the {@link CircuitBreaker} byPass
     *
     * @return boolean the byPass flag's current value
     */
    @ManagedAttribute
    @Override
    public boolean getByPassState() {
        return super.getByPassState();
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
     *
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
    @ManagedAttribute
    @Override
    public void setResetMillis(long l) {
        super.setResetMillis(l);
    }

    /**
     * Returns a {@link String} representation of the breaker's
     * status; potentially useful for exposing to monitoring software.
     *
     * @return <code>String</code> which is <code>"GREEN"</code> if
     *   the breaker is CLOSED; <code>"YELLOW"</code> if the breaker
     *   is HALF_CLOSED; and <code>"RED"</code> if the breaker is
     *   OPEN (tripped).
     */
    @ManagedAttribute
    @Override
    public String getHealthCheck() { return super.getHealthCheck(); }

    /**
     * Gets the failure tolerance limit for the {@link DefaultFailureInterpreter} that
     * comes with a {@link CircuitBreaker} by default.
     *
     * @see DefaultFailureInterpreter
     *
     * @return the number of tolerated failures in a window
     */
    @ManagedAttribute
    public int getLimit() {
        return ((DefaultFailureInterpreter) super.getFailureInterpreter()).getLimit();
    }

    /**
     * Specifies the failure tolerance limit for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *
     *  @see DefaultFailureInterpreter
     *
     *  @param limit the number of tolerated failures in a window
     */
    @ManagedAttribute
    @Override
    public void setLimit(int limit) {
        ((DefaultFailureInterpreter) super.getFailureInterpreter()).setLimit(limit);
    }

    /**
     * Gets the tolerance window in milliseconds for the {@link DefaultFailureInterpreter}
     * that comes with a {@link CircuitBreaker} by default.
     *
     * @see DefaultFailureInterpreter
     *
     * @return length of the window in milliseconds
     */
    @ManagedAttribute
    public long getWindowMillis() {
        return ((DefaultFailureInterpreter) super.getFailureInterpreter()).getWindowMillis();
    }

    /**
     * Specifies the tolerance window in milliseconds for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  CircuitBreaker} by default.
     *
     *  @see DefaultFailureInterpreter
     *
     *  @param windowMillis length of the window in milliseconds
     */
    @ManagedAttribute
    @Override
    public void setWindowMillis(long windowMillis) {
        super.setWindowMillis(windowMillis);
    }

    /**
     * returns a {@link String} representation of the breaker's
     * last known exception that caused it to OPEN (i.e. when the breaker
     * opens, it will record the specific exception that caused it to open)
     *
     * @return <code>String</code> which is the full stack trace.
     */
    @ManagedAttribute
    @Override
    public String getTripExceptionAsString() {
        return super.getTripExceptionAsString();
    }

    /**
     * Specifies whether the associated CircuitBreaker should be tripped
     * at startup time.
     *
     * @param b <code>true</code> if the CircuitBreaker should start
     *   open (tripped); <code>false</code> if the CircuitBreaker should start
     *   closed (not tripped).
     */
    public void setDisabled(boolean b) {
        disabledAtStart = b;
    }
}
