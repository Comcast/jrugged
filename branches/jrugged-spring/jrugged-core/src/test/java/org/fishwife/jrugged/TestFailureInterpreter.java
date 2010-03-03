/* Copyright 2009 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

public final class TestFailureInterpreter extends TestCase {

    public void testAcceptableException() throws Exception {
		final RuntimeException theExn = new RuntimeException();
        final Callable callable = createMock(Callable.class);
        final FailureInterpreter interpreter = createMock(FailureInterpreter.class);
        final CircuitBreaker cb = new CircuitBreaker(interpreter);

		expect(callable.call()).andThrow(theExn);
        expect(interpreter.shouldTrip(theExn)).andReturn(false);

        replay(callable);
        replay(interpreter);
        
        try {
            cb.invoke(callable);
            fail("exception expected.");
        } catch (Exception e) {}
        
        assertEquals("Status should be UP", Status.UP, cb.getStatus());
        verify(callable);
        verify(interpreter);
    }

    public void testUnacceptableException() throws Exception {
		final RuntimeException theExn = new RuntimeException();
        final Callable callable = createMock(Callable.class);
        final FailureInterpreter interpreter = createMock(FailureInterpreter.class);
        final CircuitBreaker cb = new CircuitBreaker(interpreter);

		expect(callable.call()).andThrow(theExn);
		expect(interpreter.shouldTrip(theExn))
			.andReturn(true);

        replay(callable);
        replay(interpreter);
        
        try {
            cb.invoke(callable);
            fail("exception expected.");
        } catch (Exception e) {}
        
        verify(callable);
        verify(interpreter);
        
        assertEquals("Status should be DOWN", Status.DOWN, cb.getStatus());
    }
    
}
