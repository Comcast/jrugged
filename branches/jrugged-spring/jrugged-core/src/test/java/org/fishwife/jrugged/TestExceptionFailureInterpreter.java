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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.fishwife.jrugged.circuit.CircuitBreaker;
import org.fishwife.jrugged.circuit.CircuitBreakerException;

public final class TestExceptionFailureInterpreter extends TestCase {

    public TestExceptionFailureInterpreter(String name) {
        super(name);
    }

    public void testAcceptableException() throws Exception {
        final Callable callable = createStrictMock(Callable.class);
        final FailureInterpreter interpreter = new ExceptionFailureInterpreter(
                UnsupportedOperationException.class, 2, 5L, TimeUnit.SECONDS);
        final CircuitBreaker cb = new CircuitBreaker()
                .setFailureInterpreter(interpreter);

        expect(callable.call()).andThrow(
                new UnsupportedOperationException("hi"));

        replay(callable);

        try {
            cb.invoke(callable);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {}

        verify(callable);

        this.assertStatus(cb, Status.UP);
    }

    public void testUnacceptableException() throws Exception {
        final Callable successCall = createStrictMock(Callable.class);
        final Callable failCall = createStrictMock(Callable.class);

        final long time = 250L;

        final FailureInterpreter interpreter = new ExceptionFailureInterpreter(
                UnsupportedOperationException.class, 1, time,
                TimeUnit.MILLISECONDS);
        final CircuitBreaker cb = new CircuitBreaker().setFailureInterpreter(
                interpreter).setResetMillis(time);

        expect(failCall.call()).andThrow(
                    new UnsupportedOperationException("hi"));

        expect(successCall.call()).andReturn("hello");

        replay(successCall);
        replay(failCall);

        try {
            cb.invoke(failCall);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {}

        this.assertStatus(cb, Status.DOWN);
        
        try {
            cb.invoke(successCall);
            fail("exception expected");
        } catch (CircuitBreakerException e) {}

        // waits for circuit to re-open
        Thread.sleep(time * 2);

        cb.invoke(successCall);
            
        this.assertStatus(cb, Status.UP);        

        verify(successCall);
        verify(failCall);
    }

    private void assertStatus(CircuitBreaker cb, Status status) {
        assertEquals("Status should be " + status, status, cb.getStatus());
    }
    
}
