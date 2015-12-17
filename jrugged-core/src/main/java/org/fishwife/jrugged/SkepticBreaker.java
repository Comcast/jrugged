/* SkepticBreaker.java
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

import org.fishwife.jrugged.Breaker.BreakerState;
import static org.easymock.EasyMock.createMock;

/** A {@link SkepticBreaker} can be used with a service to throttle traffic
 *  to a failed subsystem (particularly one we might not be able to monitor,
 *  such as a peer system which must be accessed over the network). Service
 *  calls are wrapped by the <code>SkepticBreaker</code>.
 *  <p>
 *  When everything is operating normally, the <code>SkepticBreaker</code>
 *  is CLOSED and the calls are allowed through. If the 'good timer' runs out
 *  while in CLOSED state, Skepticism level decreases. 
 *  <p>
 *  When a call fails, however, the <code>SkepticBreaker</code> "trips" and
 *  moves to an OPEN state. Skepticism level increases. Client calls are not 
 *  allowed through while the <code>SkepticBreaker</code> is OPEN.
 *  <p>
 *  After a certain "cooldown" period where calls are not allowed to go through 
 *  but internal sample calls are made, if no bad calls are encountered after the
 *  cooldown timer runs out (wait time), the <code>SkepticBreaker</code> will 
 *  transition back to a CLOSED state. If bad calls are made, the timer restarts.
 *  <p>
 *  Sample usage:
 *  <pre>
    public class Service implements Monitorable {
        private SkepticBreaker cb = new SkepticBreaker();
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
public class SkepticBreaker extends Breaker implements MonitoredService, ServiceWrapper {
 
    /** The time the breaker last switched into good/CLOSED state. */
    protected AtomicLong lastCloseTime = new AtomicLong(System.currentTimeMillis()); 

    /** Variables added to determine Wait Timer and Good Timer */
    protected AtomicLong waitBase = new AtomicLong(1000L);
    protected AtomicLong waitMult = new AtomicLong(100L);
    protected AtomicLong goodBase = new AtomicLong(600000L);
    protected AtomicLong goodMult = new AtomicLong(100L);
    protected AtomicLong skepticLevel = new AtomicLong(0L);
    protected AtomicLong maxLevel = new AtomicLong(20L);
    
    /** How long the cooldown period is in milliseconds during Wait Phase. */
    protected AtomicLong waitTime = new AtomicLong((long) (waitBase.get() + 
    		waitMult.get() * Math.pow(2, skepticLevel.get())));
   
    /** How long the cooldown period is in milliseconds during Good Phase. */
    protected AtomicLong goodTime = new AtomicLong((long) (goodBase.get() + 
    		goodMult.get() * Math.pow(2, skepticLevel.get())));

    /** The default name if none is provided. */
    private static final String DEFAULT_NAME = "SkepticBreaker";
    
    /** The name for the SkepticBreaker. */
    protected String name = DEFAULT_NAME;
    
    /** Creates a {@link SkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}). */
    public SkepticBreaker() {
    }
    
    /** Creates a {@link SkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link SkepticBreaker}.
     */
    public SkepticBreaker(String name) {
        super(name);
    }

    /** Creates a {@link SkepticBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public SkepticBreaker(FailureInterpreter fi) {
        super(fi);
    }

    /** Creates a {@link SkepticBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link SkepticBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public SkepticBreaker(String name, FailureInterpreter fi) {
        super(name, fi);
    }

    /** Creates a {@link SkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and using the supplied {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link SkepticBreaker}.
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public SkepticBreaker(String name, BreakerExceptionMapper<? extends Exception> mapper) {
        super(name, mapper);
    }

    /** Creates a {@link SkepticBreaker} with the provided {@link
     *  FailureInterpreter} and using the provided {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link SkepticBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public SkepticBreaker(String name,
                          FailureInterpreter fi,
                          BreakerExceptionMapper<? extends Exception> mapper) {
        super(name, fi, mapper);
    }

    /** Wrap the given service call with the {@link SkepticBreaker}
     *  protection logic.
     *  @param c the {@link Callable} to attempt
     *  @return whatever c would return on success
     *  @throws BreakerException if the
     *    breaker was OPEN and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */ 
    public <V> V invoke(Callable<V> c) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
            	if (!isHardTrip) {
            		try {
            		    Callable<Object> mockCallable = createMock(Callable.class);
            		    mockCallable.call();
            		}
            		catch (Throwable cause) {
            			handleFailure(cause); 
            		}
            	}
                throw mapException(new BreakerException());
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
        else {
            return c.call();
        }
    }

    /** Wrap the given service call with the {@link SkepticBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @throws BreakerException if the
     *    breaker was OPEN and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public void invoke(Runnable r) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
            	if(!isHardTrip){
            		try{
            			Runnable mockRunnable = createMock(Runnable.class);
            		    mockRunnable.run(); 
            		}
            		catch (Throwable cause){
            			handleFailure(cause); 
            		}
            	}
                throw mapException(new BreakerException());
            }

            try {
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

    /** Wrap the given service call with the {@link SkepticBreaker}
     *  protection logic.
     *  @param r the {@link Runnable} to attempt
     *  @param result what to return after <code>r</code> succeeds
     *  @return result
     *  @throws BreakerException if the
     *    breaker was OPEN and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if <code>c</code> throws one during
     *    execution
     */
    public <V> V invoke(Runnable r, V result) throws Exception {
        if (!byPass) {
            if (!allowRequest()) {
            	if(!isHardTrip){
            		try{
            			Runnable mockRunnable = createMock(Runnable.class);
            		    mockRunnable.run(); 
            		}
            		catch (Throwable cause){
            			handleFailure(cause); 
            		}
            	}
                throw mapException(new BreakerException());
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
        else {
            r.run();
            return result;
        }
    }

    /**
     * Causes the {@link SkepticBreaker} to trip and OPEN; no new
     *  requests will be allowed until the <code>SkepticBreaker</code>
     *  resets. Skeptic level increases and timers are updated accordingly.
     */
    public void trip() {
    	if (state != BreakerState.OPEN) {
            openCount.getAndIncrement();
        }
    	if (skepticLevel.get() < maxLevel.get()) {
    		increaseSkepticLevel();
    	}
        state = BreakerState.OPEN;
        lastFailure.set(System.currentTimeMillis());

        notifyBreakerStateChange(getStatus());
    }
    
    /**
     * Increments the skeptic level and updates timers; occurs when the 
     * {@link SkepticBreaker} trips and moves from CLOSED to OPEN
     */
    public void increaseSkepticLevel() {
    	skepticLevel.set(skepticLevel.incrementAndGet());
    	updateTimers();
    }
    
    /**
     * Updates wait timer and good timer based on current skeptic level.
     */
    public void updateTimers() {
    	waitTime.set((long) (waitBase.get() + waitMult.get() * Math.pow(2, skepticLevel.get())));
    	goodTime.set((long) (goodBase.get() + goodMult.get() * Math.pow(2, skepticLevel.get())));
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
        skepticLevel.set(0);
        updateTimers();

        notifyBreakerStateChange(getStatus());
    }

    /**
     * Get the current {@link ServiceStatus} of the
     * {@link SkepticBreaker}, including the name,
     * {@link org.fishwife.jrugged.Status}, and reason.
     * @return the {@link ServiceStatus}.
     */
    public ServiceStatus getServiceStatus() {
        if (byPass) {
            return new ServiceStatus(name, Status.DEGRADED, "Bypassed");
        }

        return (state == BreakerState.CLOSED || (lastFailure.get() > 0 
        		&& moveToClosed() && !isHardTrip) ?
            new ServiceStatus(name, Status.UP) : 
            new ServiceStatus(name, Status.DOWN, "Open"));
    }

    protected void handleFailure(Throwable cause) throws Exception {
        if (failureInterpreter == null || failureInterpreter.shouldTrip(cause)) {
            if (state == BreakerState.CLOSED) {
            	this.tripException = cause;
                trip();// also updates skeptic level
            }
            else {
            	//update last failure - this is the case where we send 'ping' that fails
            	lastFailure.set(System.currentTimeMillis());
            }
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
     * Reports a successful service call to the {@link SkepticBreaker},
     * putting the <code>SkepticBreaker</code> back into the CLOSED
     * state serving requests.
     */
    protected void close() {
        state = BreakerState.CLOSED;
        notifyBreakerStateChange(getStatus());
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
            decreaseSkepticLevel(); 
        	return true;
        }

        return moveToClosed();
    }

    private boolean moveToClosed() {
    	if (BreakerState.OPEN == state &&
            System.currentTimeMillis() - lastFailure.get() >= waitTime.get()) {
            
            state = BreakerState.CLOSED;
            notifyBreakerStateChange(getStatus());
            
            lastCloseTime.set(lastFailure.get() + waitTime.get());
            decreaseSkepticLevel(); 
            
            return true;
        }

        return false;
    }
    
    /**
     * Checks to see if SkepticLevel needs to be updated (if Good Timer has 
     * expired). If necessary, decrements the  skeptic level and updates timers; 
     * occurs when the {@link SkepticBreaker} is in the CLOSED state
     */
    protected void decreaseSkepticLevel() {
    	long currTime = System.currentTimeMillis();
    	if (currTime - lastCloseTime.get() >= goodTime.get()) {
    		if(skepticLevel.get() > 0){
	    		skepticLevel.set(skepticLevel.decrementAndGet()); 
	    		lastCloseTime.set(goodTime.get() + lastCloseTime.get()); 
	    		updateTimers(); 
	    		decreaseSkepticLevel(); 
    		}
    	}
    }

	public long getWaitBase() {
		return waitBase.get();
	}

	public void setWaitBase(long wBase) {
		waitBase.set(wBase);
	}

	public long getWaitMult() {
		return waitMult.get();
	}

	public void setWaitMult(long wMult) {
		waitMult.set(wMult);
	}

	public long getGoodBase() {
		return goodBase.get();
	}

	public void setGoodBase(long gBase) {
		goodBase.set(gBase);
	}

	public long getGoodMult() {
		return goodMult.get();
	}

	public void setGoodMult(long gMult) {
		goodMult.set(gMult);
	}
	
	public long getMaxLevel() {
		return maxLevel.get();
	}
	
	public void setMaxLevel(long mLevel) {
		maxLevel.set(mLevel);
	}

	public long getSkepticLevel() {
		return skepticLevel.get();
	}
	
	public long getWaitTime() {
		return waitTime.get();
	}
	
    public void setWaitTime(long l) {
        waitTime.set(l);
    }

	public long getGoodTime() {
		return goodTime.get();
	}
}
