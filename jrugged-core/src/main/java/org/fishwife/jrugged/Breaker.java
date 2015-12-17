
/* Breaker.java
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/** A {@link Breaker} can be used with a service to throttle traffic
 *  to a failed subsystem (particularly one we might not be able to monitor,
 *  such as a peer system which must be accessed over the network). Service
 *  calls are wrapped by the <code>Breaker</code>.
 *  <p>
 *  When everything is operating normally, the <code>Breaker</code>
 *  is CLOSED and the calls are allowed through.
 *  <p>
 *  When a call fails, however, the <code>Breaker</code> and 
 *  <code>SkepticBreaker</code> act differently. See corresponding 
 *  documentation for more details. 
 */
public abstract class Breaker implements MonitoredService, ServiceWrapper {
    /**
     * Represents whether a {@link Breaker} is OPEN, HALF_CLOSED,
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

    protected Throwable tripException = null;

    /**
     * Returns the last exception that caused the breaker to trip, NULL if never tripped.
     *
     * @return Throwable
     */
    public Throwable getTripException() {
        return tripException;
    }

    /**
     * Returns the last exception that caused the breaker to trip, empty <code>String </code>
     * if never tripped.
     *
     * @return Throwable
     */
    public String getTripExceptionAsString() {
        if (tripException == null) {
            return "";
        } else {
            return getFullStackTrace(tripException);

        }
    }

    /** Current state of the breaker. */
    protected volatile BreakerState state = BreakerState.CLOSED;

    /** The time the breaker last tripped, in milliseconds since the
        epoch. */
    protected AtomicLong lastFailure = new AtomicLong(0L);

    /** How many times the breaker has tripped during its lifetime. */
    protected AtomicLong openCount = new AtomicLong(0L);
    
    /** The {@link FailureInterpreter} to use to determine whether a
        given failure should cause the breaker to trip. */
    protected FailureInterpreter failureInterpreter =
        new DefaultFailureInterpreter();

    /** Helper class to allow throwing an application-specific
     * exception rather than the default {@link
     * BreakerException}. */
    protected BreakerExceptionMapper<? extends Exception> exceptionMapper;

    protected List<BreakerNotificationCallback> cbNotifyList =
            Collections.synchronizedList(new ArrayList<BreakerNotificationCallback>());

    protected boolean isHardTrip;

    /**
     * Bypass this Breaker - used for testing, or other operational
     * situations where verification of the Break might be required.
     */
    protected boolean byPass = false;

    /* DEFAULT NAME WAS HERE */
    /* NOTE: I still included variable 'name' in SkepticBreaker */ 
    
    /** The name for the Breaker. */
    protected String name = "Breaker";

    /** Creates a {@link Breaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}). */
    public Breaker() {
    }

    /** Creates a {@link Breaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link Breaker}.
     */
    public Breaker(String name) {
        this.name = name;
    }

    /** Creates a {@link Breaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public Breaker(FailureInterpreter fi) {
        failureInterpreter = fi;
    }

    /** Creates a {@link Breaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link Breaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public Breaker(String name, FailureInterpreter fi) {
        this.name = name;
        failureInterpreter = fi;
    }

    /** Creates a {@link Breaker} with a {@link
     *  DefaultFailureInterpreter} and using the supplied {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link Breaker}.
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public Breaker(String name, BreakerExceptionMapper<? extends Exception> mapper) {
        this.name = name;
        exceptionMapper = mapper;
    }

    /** Creates a {@link Breaker} with the provided {@link
     *  FailureInterpreter} and using the provided {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link Breaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public Breaker(String name,
                          FailureInterpreter fi,
                          BreakerExceptionMapper<? extends Exception> mapper) {
        this.name = name;
        failureInterpreter = fi;
        exceptionMapper = mapper;
    }

    /** Wrap the given service call with the {@link Breaker}
     *  protection logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public abstract <V> V invoke(Callable<V> c) throws Exception;
    
    /** Wrap the given service call with the {@link Breaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public abstract void invoke(Runnable r) throws Exception;
    
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
    public abstract <V> V invoke(Runnable r, V result) throws Exception;
    
    /**
     * When called with true - causes the {@link Breaker} to byPass
     * its functionality allowing requests to be executed unmolested
     * until the <code>Breaker</code> is reset or the byPass
     * is manually set to false.
     *
     * @param b Set this breaker into bypass mode
     */
    public void setByPassState(boolean b) {
        byPass = b;
        notifyBreakerStateChange(getStatus());
    }

    /**
     * Get the current state of the {@link Breaker} byPass
     *
     * @return boolean the byPass flag's current value
     */
    public boolean getByPassState() {
        return byPass;
    }

    /**
     * Causes the {@link Breaker} to trip and OPEN; no new
     *  requests will be allowed until the <code>Breaker</code>
     *  resets.
     */
    public abstract void trip(); 

    /**
     * Manually trips the Breaker until {@link #reset()} is invoked.
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
        return lastFailure.get();
    }

    /**
     * Returns the number of times the breaker has tripped OPEN during
     * its lifetime.
     * @return long the number of times the circuit breaker tripped
     */
    public long getTripCount() {
        return openCount.get();
    }

    /**
     * Manually set the breaker to be reset and ready for use.  This
     * is only useful after a manual trip otherwise the breaker will
     * trip automatically again if the service is still unavailable.
     * Just like a real breaker.  WOOT!!!
     */
    public abstract void reset();

    /**
     * Returns the current {@link org.fishwife.jrugged.Status} of the
     *  {@link Breaker}.  In this case, it really refers to the
     *  status of the client service.  If the
     *  <code>Breaker</code> is CLOSED, we report that the
     *  client is UP; if it is HALF_CLOSED, we report that the client
     *  is DEGRADED; if it is OPEN, we report the client is DOWN.
     *
     *  @return Status the current status of the breaker
     */
    public Status getStatus() {
        return getServiceStatus().getStatus();
    }

    /**
     * Get the current {@link ServiceStatus} of the
     * {@link CircuitBreaker}, including the name,
     * {@link org.fishwife.jrugged.Status}, and reason.
     * @return the {@link ServiceStatus}.
     */
    public abstract ServiceStatus getServiceStatus();
    
    /* ----GET RESET MILLIS WAS HERE ---*/
    /* ----SET RESET MILLIS WAS HERE ---*/

    /** Returns a {@link String} representation of the breaker's
     * status; potentially useful for exposing to monitoring software.
     * @return <code>String</code> which is <code>"GREEN"</code> if
     *   the breaker is CLOSED; <code>"YELLOW"</code> if the breaker
     *   is HALF_CLOSED; and <code>"RED"</code> if the breaker is
     *   OPEN (tripped). */
    public String getHealthCheck() {
        return getStatus().getSignal();
    }

    /**
     * Specifies the failure tolerance limit for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  Breaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param limit the number of tolerated failures in a window
     */
    public void setLimit(int limit) {
        FailureInterpreter fi = getFailureInterpreter();
        if (!(fi instanceof DefaultFailureInterpreter)) {
            throw new IllegalStateException("setLimit() not supported: this Breaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
        }
        ((DefaultFailureInterpreter)fi).setLimit(limit);
    }

    /**
     * Specifies a set of {@link Throwable} classes that should not
     *  be considered failures by the {@link Breaker}.
     *  @see DefaultFailureInterpreter
     *  @param ignore a {@link java.util.Collection} of {@link Throwable}
     *  classes
     */
    public void setIgnore(Collection<Class<? extends Throwable>> ignore) {
        FailureInterpreter fi = getFailureInterpreter();
        if (!(fi instanceof DefaultFailureInterpreter)) {
            throw new IllegalStateException("setIgnore() not supported: this Breaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
        }

        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] classes = new Class[ignore.size()];
        int i = 0;
        for(Class<? extends Throwable> c : ignore) {
            classes[i] = c;
            i++;
        }
        ((DefaultFailureInterpreter)fi).setIgnore(classes);
    }

    /**
     * Specifies the tolerance window in milliseconds for the {@link
     *  DefaultFailureInterpreter} that comes with a {@link
     *  Breaker} by default.
     *  @see DefaultFailureInterpreter
     *  @param windowMillis length of the window in milliseconds
     */
    public void setWindowMillis(long windowMillis) {
        FailureInterpreter fi = getFailureInterpreter();
        if (!(fi instanceof DefaultFailureInterpreter)) {
            throw new IllegalStateException("setWindowMillis() not supported: this Breaker's FailureInterpreter isn't a DefaultFailureInterpreter.");
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
     * A helper that converts BreakerExceptions into a known
     * 'application' exception.
     *
     * @param mapper my converter object
     */
    public void setExceptionMapper(BreakerExceptionMapper<? extends Exception> mapper) {
        this.exceptionMapper = mapper;
    }

    /**
     * Add an interested party for {@link Breaker} events, like up,
     * down, degraded status state changes.
     *
     * @param listener an interested party for {@link Breaker} status events.
     */
    public void addListener(BreakerNotificationCallback listener) {
        cbNotifyList.add(listener);
    }

    /**
     * Set a list of interested parties for {@link Breaker} events, like up,
     * down, degraded status state changes.
     *
     * @param listeners a list of interested parties for {@link Breaker} status events.
     */
    public void setListeners(ArrayList<BreakerNotificationCallback> listeners) {
        cbNotifyList = Collections.synchronizedList(listeners);
    }

    /**
     * Get the helper that converts {@link BreakerException}s into
     * application-specific exceptions.
     * @return {@link BreakerExceptionMapper} my converter object, or
     *   <code>null</code> if one is not currently set.
     */
    public BreakerExceptionMapper<? extends Exception> getExceptionMapper(){
        return this.exceptionMapper;
    }

    protected Exception mapException(BreakerException cbe) {
        if (exceptionMapper == null)
            return cbe;

        return exceptionMapper.map(this, cbe);
    }

    protected abstract void handleFailure(Throwable cause) throws Exception;

    /**
     * Reports a successful service call to the {@link Breaker},
     * putting the <code>Breaker</code> back into the CLOSED
     * state serving requests.
     */
    protected abstract void close();

    /* canAttempt() was here */ 

    protected void notifyBreakerStateChange(Status status) {
        if (cbNotifyList != null && cbNotifyList.size() >= 1) {
            for (BreakerNotificationCallback notifyObject : cbNotifyList) {
                notifyObject.notify(status);
            }
        }
    }

    /**
     * @return boolean whether the breaker will allow a request
     * through or not.
     */
    protected abstract boolean allowRequest();

    private String getFullStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
