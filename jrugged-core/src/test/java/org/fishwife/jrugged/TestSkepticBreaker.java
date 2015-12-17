/* TestSkepticBreaker.java
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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSkepticBreaker {
    private SkepticBreaker impl;
    private Callable<Object> mockCallable;
    private Runnable mockRunnable;

    Status theStatus;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        impl = new SkepticBreaker();
        mockCallable = createMock(Callable.class);
        mockRunnable = createMock(Runnable.class);
    }

    @Test
    public void testInvokeWithRunnableResultAndResultReturnsResult() throws Exception {
        final Object result = new Object();

        mockRunnable.run();
        replay(mockRunnable);

        Object theReturned = impl.invoke(mockRunnable, result);

        verify(mockRunnable);
        assertSame(result, theReturned);
    }

    @Test
    public void testInvokeWithRunnableResultAndByPassReturnsResult() throws Exception {
        final Object result = new Object();
        impl.setByPassState(true);

        mockRunnable.run();
        replay(mockRunnable);

        Object theReturned = impl.invoke(mockRunnable, result);

        verify(mockRunnable);
        assertSame(result, theReturned);
    }

    @Test(expected = BreakerException.class)
    public void testInvokeWithRunnableResultAndTripHardReturnsException() throws Exception {
        final Object result = new Object();
        impl.tripHard();

        mockRunnable.run();
        replay(mockRunnable);

        impl.invoke(mockRunnable, result);

        verify(mockRunnable);
    }

    @Test
    public void testInvokeWithRunnableDoesNotError() throws Exception {
        mockRunnable.run();
        replay(mockRunnable);

        impl.invoke(mockRunnable);

        verify(mockRunnable);
    }

    @Test
    public void testInvokeWithRunnableAndByPassDoesNotError() throws Exception {
        impl.setByPassState(true);

        mockRunnable.run();
        replay(mockRunnable);

        impl.invoke(mockRunnable);

        verify(mockRunnable);
    }

    @Test(expected = BreakerException.class)
    public void testInvokeWithRunnableAndTripHardReturnsException() throws Exception {
        impl.tripHard();

        mockRunnable.run();
        replay(mockRunnable);

        impl.invoke(mockRunnable);

        verify(mockRunnable);
    }

    @Test
    public void testStaysClosedOnSuccess() throws Exception {
        impl.state = SkepticBreaker.BreakerState.CLOSED;
        final Object obj = new Object();
        expect(mockCallable.call()).andReturn(obj);
        replay(mockCallable);

        Object result = impl.invoke(mockCallable);

        verify(mockCallable);
        assertSame(obj, result);
        assertEquals(SkepticBreaker.BreakerState.CLOSED, impl.state);
    }

    @Test
    //Add check on timers here as well
    public void testOpensOnFailure() throws Exception {
        long start = System.currentTimeMillis();
        impl.state = SkepticBreaker.BreakerState.OPEN;
        expect(mockCallable.call()).andThrow(new RuntimeException());
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown an exception");
        } catch (RuntimeException expected) {
        }

        long end = System.currentTimeMillis();

        verify(mockCallable);
        assertEquals(SkepticBreaker.BreakerState.OPEN, impl.state);
        assertTrue(impl.lastFailure.get() >= start);
        assertTrue(impl.lastFailure.get() <= end);
    }

    @Test
    public void testOpenDuringCooldownThrowsCBException()
            throws Exception {

        impl.state = SkepticBreaker.BreakerState.OPEN;
        impl.lastFailure.set(System.currentTimeMillis());
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown an exception");
        } catch (BreakerException expected) {
        }

        verify(mockCallable);
        assertEquals(SkepticBreaker.BreakerState.OPEN, impl.state);
    }

    @Test
    public void testClosedAfterWaitTimeExpires() //testOpenAfterCooldownGoesClosed()
            throws Exception {
        impl.state = SkepticBreaker.BreakerState.OPEN;
        impl.waitTime.set(1000);// waitTime is 1100 by default. set to 1000 in case default changes
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        assertEquals(Status.UP, impl.getStatus());
        assertEquals(SkepticBreaker.BreakerState.CLOSED, impl.state);
    }

    @Test
    public void testOpenFailureOpensAgain()
            throws Exception {

        impl.state = SkepticBreaker.BreakerState.OPEN;
        impl.waitTime.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        long start = System.currentTimeMillis();

        expect(mockCallable.call()).andThrow(new RuntimeException());
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown exception");
        } catch (RuntimeException expected) {
        }

        long end = System.currentTimeMillis();

        verify(mockCallable);
        assertEquals(SkepticBreaker.BreakerState.OPEN, impl.state);
        assertTrue(impl.lastFailure.get() >= start);
        assertTrue(impl.lastFailure.get() <= end);
    }

    @Test
    public void testManualTripAndReset() throws Exception {
        impl.state = SkepticBreaker.BreakerState.OPEN;
        final Object obj = new Object();
        expect(mockCallable.call()).andReturn(obj);
        replay(mockCallable);

        impl.trip();
        try {
            impl.invoke(mockCallable);
            fail("Manual trip method failed.");
        } catch (BreakerException e) {
        }

        impl.reset();

        Object result = impl.invoke(mockCallable);

        verify(mockCallable);
        assertSame(obj, result);
        assertEquals(SkepticBreaker.BreakerState.CLOSED, impl.state);
    }

    @Test
    public void testTripHard() throws Exception {
        expect(mockCallable.call()).andReturn("hi");

        replay(mockCallable);

        impl.tripHard();
        try {
            impl.invoke(mockCallable);
            fail("exception expected after SkepticBreaker.tripHard()");
        } catch (BreakerException e) {
        }
        assertEquals(SkepticBreaker.BreakerState.OPEN, impl.state);

        impl.reset();
        impl.invoke(mockCallable);
        assertEquals(SkepticBreaker.BreakerState.CLOSED, impl.state);

        verify(mockCallable);
    }

    @Test
    public void testGetTripCount() throws Exception {
        long tripCount1 = impl.getTripCount();

        impl.tripHard();
        long tripCount2 = impl.getTripCount();
        assertEquals(tripCount1 + 1, tripCount2);

        impl.tripHard();
        assertEquals(tripCount2, impl.getTripCount());
    }

    @Test
    public void testGetStatusWhenOpen() {
        impl.state = SkepticBreaker.BreakerState.OPEN;
        Assert.assertEquals(Status.DOWN, impl.getStatus());
    }

    @Test
    //CHANGED THIS (title), must change test to reflect
    public void testGetStatusWhenClosedBeforeReset() {
        impl.state = SkepticBreaker.BreakerState.CLOSED;
        impl.waitTime.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 50);

        assertEquals(Status.UP, impl.getStatus());
    }

    @Test
    public void testGetStatusWhenOpenAfterExpiration() {
        impl.state = SkepticBreaker.BreakerState.OPEN;
        impl.waitTime.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        assertEquals(Status.UP, impl.getStatus());
    }

    @Test
    public void testGetStatusAfterHardTrip() {
        impl.tripHard();
        impl.waitTime.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        assertEquals(Status.DOWN, impl.getStatus());
    }

    @Test
    public void testStatusIsByPassWhenSet() {
        impl.setByPassState(true);
        assertEquals(Status.DEGRADED, impl.getStatus());
    }

    @Test
    public void testByPassIgnoresCurrentBreakerStateWhenSet() {
        impl.state = SkepticBreaker.BreakerState.OPEN;
        assertEquals(Status.DOWN, impl.getStatus());

        impl.setByPassState(true);
        assertEquals(Status.DEGRADED, impl.getStatus());

        impl.setByPassState(false);
        assertEquals(Status.DOWN, impl.getStatus());
    }

    @Test
    public void testByPassIgnoresBreakerStateAndCallsWrappedMethod() throws Exception {
        expect(mockCallable.call()).andReturn("hi").anyTimes();

        replay(mockCallable);

        impl.tripHard();
        impl.setByPassState(true);

        try {
            impl.invoke(mockCallable);
        } catch (BreakerException e) {
            fail("exception not expected when SkepticBreaker is bypassed.");
        }
        assertEquals(SkepticBreaker.BreakerState.OPEN, impl.state);
        assertEquals(Status.DEGRADED, impl.getStatus());

        impl.reset();
        impl.setByPassState(false);
        impl.invoke(mockCallable);
        assertEquals(SkepticBreaker.BreakerState.CLOSED, impl.state);

        verify(mockCallable);
    }

    @Test
    public void testNotificationCallback() throws Exception {

        BreakerNotificationCallback cb = new BreakerNotificationCallback() {
            public void notify(Status s) {
                theStatus = s;
            }
        };

        impl.addListener(cb);
        impl.trip();

        assertNotNull(theStatus);
        assertEquals(Status.DOWN, theStatus);
    }

    @Test(expected = Throwable.class)
    public void SkepticBreakerKeepsExceptionThatTrippedIt() throws Throwable {

        try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }

        Throwable tripException = impl.getTripException();
        assertEquals("broken", tripException.getMessage());
        throw tripException;
    }

    @Test(expected = Throwable.class)
    public void resetSkepticBreakerStillHasTripException() throws Throwable {

        try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }
        impl.reset();

        Throwable tripException = impl.getTripException();
        assertEquals("broken", tripException.getMessage());
        throw tripException;
    }

    @Test
    public void skepticBreakerReturnsExceptionAsString() {

        try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }

        String s = impl.getTripExceptionAsString();
        assertTrue(s.startsWith("java.lang.Exception: broken"));
        assertTrue(s.contains("at org.fishwife.jrugged.TestSkepticBreaker$FailingCallable.call"));
        assertTrue(s.contains("Caused by: java.lang.Exception: The Cause"));
    }

    @Test
    public void neverTrippedSkepticBreakerReturnsNullForTripException() throws Exception {

        impl.invoke(mockCallable);

        Throwable tripException = impl.getTripException();

        assertNull(tripException);
    }
    
    @Test
    public void testResetsToInitialTimerValues() throws Exception {
    	long expectedWaitTime = impl.getWaitTime();
    	long expectedGoodTime = impl.getGoodTime();
    	long expectedSkepticLevel = impl.getSkepticLevel();
    	
    	try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }
        impl.reset();
        
        assertEquals(expectedWaitTime, impl.getWaitTime());
        assertEquals(expectedGoodTime, impl.getGoodTime());
        assertEquals(expectedSkepticLevel, impl.getSkepticLevel());
    }
    
    @Test
    public void testTimerValuesAndSkepticLevelAfterOneTrip() throws Exception {
    	long expectedWaitTime = (long) (impl.getWaitBase() + 
        		impl.getWaitMult() * Math.pow(2, impl.getSkepticLevel() + 1));
    	long expectedGoodTime = (long) (impl.getGoodBase() + 
        		impl.getGoodMult() * Math.pow(2, impl.getSkepticLevel() + 1));
    	long expectedSkepticLevel = impl.getSkepticLevel() + 1;
    	
    	try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }
        
        assertEquals(expectedWaitTime, impl.getWaitTime());
        assertEquals(expectedGoodTime, impl.getGoodTime());
        assertEquals(expectedSkepticLevel, impl.getSkepticLevel());
    }
    
    @Test
    public void testTimerValuesAndSkepticLevelAfterThreeTrips() throws Exception {
    	int numTrips = 3;
    	long expectedWaitTime = (long) (impl.getWaitBase() + 
        		impl.getWaitMult() * Math.pow(2, impl.getSkepticLevel() + numTrips));
    	long expectedGoodTime = (long) (impl.getGoodBase() + 
        		impl.getGoodMult() * Math.pow(2, impl.getSkepticLevel() + numTrips));
    	long expectedSkepticLevel = impl.getSkepticLevel() + numTrips;
    	
    	for (int i = 0; i < numTrips; i++) {
    		try {
                impl.invoke(new FailingCallable("broken"));
            } catch (Exception e) {

            }
    		Thread.sleep(impl.getWaitTime() + 100);
    	}
        
        assertEquals(expectedWaitTime, impl.getWaitTime());
        assertEquals(expectedGoodTime, impl.getGoodTime());
        assertEquals(expectedSkepticLevel, impl.getSkepticLevel());
    }
    
    @Test
    public void testSkepticLevelStopsAtMaxLevel() throws Exception {
    	long expectedWaitTime = (long) (impl.getWaitBase() + 
        		impl.getWaitMult() * Math.pow(2, impl.getMaxLevel()));
    	long expectedGoodTime = (long) (impl.getGoodBase() + 
        		impl.getGoodMult() * Math.pow(2, impl.getMaxLevel()));
    	long expectedSkepticLevel = impl.getMaxLevel();
    	impl.skepticLevel.set(impl.getMaxLevel());
    	impl.updateTimers();
    	
		try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }
		
        assertEquals(expectedWaitTime, impl.getWaitTime());
        assertEquals(expectedGoodTime, impl.getGoodTime());
        assertEquals(expectedSkepticLevel, impl.getSkepticLevel());
    }
    
    @Test
    public void testDecreaseSkepticLevel() throws Exception {
    	impl.setGoodBase(100L);
    	impl.setWaitBase(100L);
    	impl.updateTimers();
    	
    	long expectedWaitTime = impl.getWaitTime();
    	long expectedGoodTime = impl.getGoodTime();
    	long expectedSkepticLevel = impl.getSkepticLevel();
    	
    	try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }
    	Thread.sleep(impl.getWaitTime() + 100);
    	Thread.sleep(impl.getGoodTime() + 100);
    	
    	assertEquals(Status.UP, impl.getStatus());
    	assertEquals(expectedWaitTime, impl.getWaitTime());
        assertEquals(expectedGoodTime, impl.getGoodTime());
        assertEquals(expectedSkepticLevel, impl.getSkepticLevel());
    }
    
    // test bad ping - not sure how?

    private class FailingCallable implements Callable<Object> {

        private final String exceptionMessage;

        public FailingCallable(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }

        Exception causeException = new Exception("The Cause");

        public Object call() throws Exception {
            throw new Exception(exceptionMessage, causeException);
        }
    }

}
