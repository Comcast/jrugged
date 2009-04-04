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

public class TestCircuitBreaker extends TestCase {
    private CircuitBreaker impl;

    public void setUp() {
	impl = new CircuitBreaker();
    }

    public void testStaysOpenOnSuccess() throws Exception {
	impl.state = CircuitBreaker.BreakerState.OPEN;
	final Object obj = new Object();
	Object result = impl.invoke(new Callable() {
		public Object call() {
		    return obj;
		}
	    });
	assertSame(obj, result);
	assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    public void testTripsOnFailure() throws Exception {
	long start = System.currentTimeMillis();
	impl.state = CircuitBreaker.BreakerState.OPEN;
	try {
	    Object result = impl.invoke(new Callable() {
		    public Object call() {
			throw new RuntimeException();
		    }
		});
	    fail("should have thrown an exception");
	} catch (RuntimeException expected) {
	}
	long end = System.currentTimeMillis();
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
	assertTrue(impl.lastFailure >= start);
	assertTrue(impl.lastFailure <= end);
    }

    public void testHalfOpenWithLiveAttemptThrowsCBException() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.HALF_OPEN;
	impl.isAttemptLive = true;
	final Object obj = new Object();
	try {
	    Object result = impl.invoke(new Callable() {
		    public Object call() {
			return obj;
		    }
		});
	    fail("should have thrown an exception");
	} catch (CircuitBreakerException expected) {
	}
	assertEquals(CircuitBreaker.BreakerState.HALF_OPEN, impl.state);
	assertTrue(impl.isAttemptLive);
    }

    public void testClosedDuringCooldownThrowsCBException() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.lastFailure = System.currentTimeMillis();
	final Object obj = new Object();
	try {
	    Object result = impl.invoke(new Callable() {
		    public Object call() {
			return obj;
		    }
		});
	    fail("should have thrown an exception");
	} catch (CircuitBreakerException expected) {
	}
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
    }

    public void testClosedAfterCooldownGoesHalfOpen() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.resetMillis = 1000;
	impl.lastFailure = System.currentTimeMillis() - 2000;
	final Object obj = new Object();
	final CircuitBreaker cb = impl;
	Object result = impl.invoke(new Callable() {
		public Object call() {
		    assertEquals(CircuitBreaker.BreakerState.HALF_OPEN,
				 cb.state);
		    return obj;
		}
	    });
	assertEquals(CircuitBreaker.BreakerState.OPEN, impl.state);
    }

    public void testHalfOpenFailureClosesAgain() 
	throws Exception {

	impl.state = CircuitBreaker.BreakerState.CLOSED;
	impl.resetMillis = 1000;
	impl.lastFailure = System.currentTimeMillis() - 2000;

	long start = System.currentTimeMillis();
	final Object obj = new Object();
	final CircuitBreaker cb = impl;
	try {
	    Object result = impl.invoke(new Callable() {
		    public Object call() {
			assertEquals(CircuitBreaker.BreakerState.HALF_OPEN,
				     cb.state);
			throw new RuntimeException();
		    }
		});
	    fail("should have thrown exception");
	} catch (RuntimeException expected) {
	}
	long end = System.currentTimeMillis();
	assertEquals(CircuitBreaker.BreakerState.CLOSED, impl.state);
	assertTrue(impl.lastFailure >= start);
	assertTrue(impl.lastFailure <= end);
    }
}