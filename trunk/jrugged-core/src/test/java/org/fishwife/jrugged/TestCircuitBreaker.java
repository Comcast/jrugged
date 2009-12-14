/* TestCircuitBreaker.java
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

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

public class TestCircuitBreaker extends TestCase {
    private CircuitBreaker impl;
    private Callable mockCallable;

    public void setUp() {
	impl = new CircuitBreaker();
	mockCallable = createMock(Callable.class);
    }

    public void testStaysOpenOnSuccess() throws Exception {
	impl.state = CircuitBreaker.BreakerState.OPEN;
	final Object obj = new Object();
	expect(mockCallable.call()).andReturn(obj);
	replay(mockCallable);

	Object result = impl.invoke(mockCallable);

	verify(mockCallable);
	assertSame(obj, result);
	assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    public void testTripsOnFailure() throws Exception {
	long start = System.currentTimeMillis();
	impl.state = CircuitBreaker.BreakerState.OPEN;
	expect(mockCallable.call()).andThrow(new RuntimeException());
	replay(mockCallable);

	try {
	    Object result = impl.invoke(mockCallable);
	    fail("should have thrown an exception");
	} catch (RuntimeException expected) {
	}
	long end = System.currentTimeMillis();

	verify(mockCallable);
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
	assertTrue(impl.lastFailure >= start);
	assertTrue(impl.lastFailure <= end);
    }

    public void testHalfOpenWithLiveAttemptThrowsCBException() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.HALF_OPEN;
	impl.isAttemptLive = true;
	final Object obj = new Object();
	replay(mockCallable);

	try {
	    Object result = impl.invoke(mockCallable);
	    fail("should have thrown an exception");
	} catch (CircuitBreakerException expected) {
	}
	
	verify(mockCallable);
	assertEquals(CircuitBreaker.BreakerState.HALF_OPEN, impl.state);
	assertTrue(impl.isAttemptLive);
    }

    public void testClosedDuringCooldownThrowsCBException() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.lastFailure = System.currentTimeMillis();
	final Object obj = new Object();
	replay(mockCallable);

	try {
	    Object result = impl.invoke(mockCallable);
	    fail("should have thrown an exception");
	} catch (CircuitBreakerException expected) {
	}

	verify(mockCallable);
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
    }

    public void testClosedAfterCooldownGoesHalfOpen() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.resetMillis = 1000;
	impl.lastFailure = System.currentTimeMillis() - 2000;
	final Object obj = new Object();
	expect(mockCallable.call()).andReturn(obj);
	replay(mockCallable);
	final CircuitBreaker cb = impl;
	Object result = impl.invoke(mockCallable);

	verify(mockCallable);
	assertSame(obj, result);
	assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    public void testHalfOpenFailureClosesAgain() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.resetMillis = 1000;
	impl.lastFailure = System.currentTimeMillis() - 2000;

	long start = System.currentTimeMillis();
	final CircuitBreaker cb = impl;
	expect(mockCallable.call()).andThrow(new RuntimeException());
	replay(mockCallable);

	try {
	    Object result = impl.invoke(mockCallable);
	    fail("should have thrown exception");
	} catch (RuntimeException expected) {
	}
	long end = System.currentTimeMillis();

	verify(mockCallable);
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
	assertTrue(impl.lastFailure >= start);
	assertTrue(impl.lastFailure <= end);
    }

    public void testManualTripAndReset() throws Exception {
        impl.state = CircuitBreaker.BreakerState.CLOSED;
        final Object obj = new Object();
        expect(mockCallable.call()).andReturn(obj);
        replay(mockCallable);

        impl.trip();
        try {
            impl.invoke(mockCallable);
            fail("Manual trip method failed.");
        } catch(CircuitBreakerException e){}

        impl.reset();

        Object result = impl.invoke(mockCallable);

        verify(mockCallable);
        assertSame(obj, result);
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    public void testTripHard() throws Exception {
        expect(mockCallable.call()).andReturn("hi");
        
        replay(mockCallable);
        
        impl.tripHard();
        try {
        impl.invoke(mockCallable);
            fail("exception expected after CircuitBreaker.tripHard()");
        } catch (CircuitBreakerException e) {}
        assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
        
        impl.reset();
        impl.invoke(mockCallable);
        assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
        
        verify(mockCallable);
        
        
    }

}
