package org.fishwife.jrugged;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

public final class TestFailureInterpreter extends TestCase {

    public void testAcceptableException() throws Exception {
        final Callable callable = createMock(Callable.class);
        final FailureInterpreter interpreter = createMock(FailureInterpreter.class);
        final CircuitBreaker cb = new CircuitBreaker().setFailureInterpreter(interpreter);

        expect(interpreter.invoke(callable)).andThrow(
                new CircuitShouldStayOpenException(new RuntimeException("hi")));

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
        final Callable callable = createMock(Callable.class);
        final FailureInterpreter interpreter = createMock(FailureInterpreter.class);
        final CircuitBreaker cb = new CircuitBreaker().setFailureInterpreter(interpreter);

        expect(interpreter.invoke(callable)).andThrow(new RuntimeException("hi"));

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
