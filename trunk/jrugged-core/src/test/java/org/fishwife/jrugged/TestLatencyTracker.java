/* LatencyTracker.java
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

public class TestLatencyTracker extends TestCase {
    private LatencyTracker impl;

    public void setUp() { 
	impl = new LatencyTracker(); 
    }

    public void testCallableSuccess() throws Exception {
	final Object o = new Object();
	
	Object result = impl.invoke(new Callable() {
		public Object call() throws Exception {
		    Thread.sleep(1);
		    return o;
		}
	    });

	assertSame(result, o);
	assertTrue(impl.getLastSuccessMillis() > 0);
	assertEquals(0, impl.getLastFailureMillis());
    }

    public void testCallableFailure() throws Exception {
	
	try {
	    Object result = impl.invoke(new Callable() {
		    public Object call() throws Exception {
			Thread.sleep(1);
			throw new Exception();
		    }
		});
	    fail("should have thrown exception");
	} catch (Exception expected) {
	}
	assertTrue(impl.getLastFailureMillis() > 0);
	assertEquals(0, impl.getLastSuccessMillis());
    }


}