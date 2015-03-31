/* TestCircuitBreaker.java
 * 
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

public class TestCircuitBreaker {
    private CircuitBreaker impl;
    private Callable<Object> mockCallable;
    private Runnable mockRunnable;

    Status theStatus;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        impl = new CircuitBreaker();
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

    @Test(expected = CircuitBreakerException.class)
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

    @Test(expected = CircuitBreakerException.class)
    public void testInvokeWithRunnableAndTripHardReturnsException() throws Exception {
        impl.tripHard();

        mockRunnable.run();
        replay(mockRunnable);

        impl.invoke(mockRunnable);

        verify(mockRunnable);
    }

    @Test
    public void testStaysClosedOnSuccess() throws Exception {
        impl.state = CircuitBreaker.BreakerState.CLOSED;
        final Object obj = new Object();
        expect(mockCallable.call()).andReturn(obj);
        replay(mockCallable);

        Object result = impl.invoke(mockCallable);

        verify(mockCallable);
        assertSame(obj, result);
        assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
    }

    @Test
    public void testOpensOnFailure() throws Exception {
        long start = System.currentTimeMillis();
        impl.state = CircuitBreaker.BreakerState.OPEN;
        expect(mockCallable.call()).andThrow(new RuntimeException());
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown an exception");
        } catch (RuntimeException expected) {
        }

        long end = System.currentTimeMillis();

        verify(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
        assertTrue(impl.lastFailure.get() >= start);
        assertTrue(impl.lastFailure.get() <= end);
    }

    @Test
    public void testHalfClosedWithLiveAttemptThrowsCBException()
            throws Exception {

        impl.state = CircuitBreaker.BreakerState.HALF_CLOSED;
        impl.isAttemptLive = true;
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown an exception");
        } catch (CircuitBreakerException expected) {
        }

        verify(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.HALF_CLOSED, impl.state);
        assertTrue(impl.isAttemptLive);
    }

    @Test
    public void testOpenDuringCooldownThrowsCBException()
            throws Exception {

        impl.state = CircuitBreaker.BreakerState.OPEN;
        impl.lastFailure.set(System.currentTimeMillis());
        replay(mockCallable);

        try {
            impl.invoke(mockCallable);
            fail("should have thrown an exception");
        } catch (CircuitBreakerException expected) {
        }

        verify(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    @Test
    public void testOpenAfterCooldownGoesHalfClosed()
            throws Exception {

        impl.state = CircuitBreaker.BreakerState.OPEN;
        impl.resetMillis.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        assertEquals(Status.DEGRADED, impl.getStatus());
        assertEquals(CircuitBreaker.BreakerState.HALF_CLOSED, impl.state);
    }

    @Test
    public void testHalfClosedFailureOpensAgain()
            throws Exception {

        impl.state = CircuitBreaker.BreakerState.HALF_CLOSED;
        impl.resetMillis.set(1000);
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
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
        assertTrue(impl.lastFailure.get() >= start);
        assertTrue(impl.lastFailure.get() <= end);
    }

    @Test
    public void testGetStatusNotUpdatingIsAttemptLive() throws Exception {

        impl.trip();
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
        assertEquals(false, impl.isAttemptLive);

        impl.resetMillis.set(50);
        Thread.sleep(100);

        // The getStatus()->canAttempt() call also updated isAttemptLive to true
        assertEquals(Status.DEGRADED.getValue(), impl.getStatus().getValue());
        assertEquals(false, impl.isAttemptLive);
    }

    @Test
    public void testManualTripAndReset() throws Exception {
        impl.state = CircuitBreaker.BreakerState.OPEN;
        final Object obj = new Object();
        expect(mockCallable.call()).andReturn(obj);
        replay(mockCallable);

        impl.trip();
        try {
            impl.invoke(mockCallable);
            fail("Manual trip method failed.");
        } catch (CircuitBreakerException e) {
        }

        impl.reset();

        Object result = impl.invoke(mockCallable);

        verify(mockCallable);
        assertSame(obj, result);
        assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
    }

    @Test
    public void testTripHard() throws Exception {
        expect(mockCallable.call()).andReturn("hi");

        replay(mockCallable);

        impl.tripHard();
        try {
            impl.invoke(mockCallable);
            fail("exception expected after CircuitBreaker.tripHard()");
        } catch (CircuitBreakerException e) {
        }
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);

        impl.reset();
        impl.invoke(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);

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
        impl.state = CircuitBreaker.BreakerState.OPEN;
        Assert.assertEquals(Status.DOWN, impl.getStatus());
    }

    @Test
    public void testGetStatusWhenHalfClosed() {
        impl.state = CircuitBreaker.BreakerState.HALF_CLOSED;
        assertEquals(Status.DEGRADED, impl.getStatus());
    }

    @Test
    public void testGetStatusWhenOpenBeforeReset() {
        impl.state = CircuitBreaker.BreakerState.CLOSED;
        impl.resetMillis.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 50);

        assertEquals(Status.UP, impl.getStatus());
    }

    @Test
    public void testGetStatusWhenOpenAfterReset() {
        impl.state = CircuitBreaker.BreakerState.OPEN;
        impl.resetMillis.set(1000);
        impl.lastFailure.set(System.currentTimeMillis() - 2000);

        assertEquals(Status.DEGRADED, impl.getStatus());
    }

    @Test
    public void testGetStatusAfterHardTrip() {
        impl.tripHard();
        impl.resetMillis.set(1000);
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
        impl.state = CircuitBreaker.BreakerState.OPEN;
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
        } catch (CircuitBreakerException e) {
            fail("exception not expected when CircuitBreaker is bypassed.");
        }
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
        assertEquals(Status.DEGRADED, impl.getStatus());

        impl.reset();
        impl.setByPassState(false);
        impl.invoke(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);

        verify(mockCallable);
    }

    @Test
    public void testNotificationCallback() throws Exception {

        CircuitBreakerNotificationCallback cb = new CircuitBreakerNotificationCallback() {
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
    public void circuitBreakerKeepsExceptionThatTrippedIt() throws Throwable {

        try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }

        Throwable tripException = impl.getTripException();
        assertEquals("broken", tripException.getMessage());
        throw tripException;
    }

    @Test(expected = Throwable.class)
    public void resetCircuitBreakerStillHasTripException() throws Throwable {

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
    public void circuitBreakerReturnsExceptionAsString() {

        try {
            impl.invoke(new FailingCallable("broken"));
        } catch (Exception e) {

        }

        Throwable tripException = impl.getTripException();

        String s = impl.getTripExceptionAsString();

        assertTrue(impl.getTripExceptionAsString().startsWith("java.lang.Exception: broken\n"));
        assertTrue(impl.getTripExceptionAsString().contains("at org.fishwife.jrugged.TestCircuitBreaker$FailingCallable.call"));
        assertTrue(impl.getTripExceptionAsString().contains("Caused by: java.lang.Exception: The Cause\n"));
    }

    @Test
    public void neverTrippedCircuitBreakerReturnsNullForTripException() throws Exception {

        impl.invoke(mockCallable);

        Throwable tripException = impl.getTripException();

        assertNull(tripException);
    }

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
