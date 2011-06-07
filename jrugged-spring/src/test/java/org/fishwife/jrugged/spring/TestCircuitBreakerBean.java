package org.fishwife.jrugged.spring;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.fishwife.jrugged.CircuitBreakerException;
import org.junit.Before;
import org.junit.Test;


public class TestCircuitBreakerBean {

    private CircuitBreakerBean impl;
    
    @Before
    public void setUp() {
        impl = new CircuitBreakerBean();
    }
    
    @Test
    public void startsEnabled() throws Exception {
        final Object out = new Object();
        Callable<Object> c = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
        assertSame(out, impl.invoke(c));
    }
    
    @Test
    public void canBeDisabled() throws Exception {
        final Object out = new Object();
        Callable<Object> c = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
        impl.setDisabled(true);
        impl.afterPropertiesSet();
        try {
            impl.invoke(c);
            fail("Should have thrown CircuitBreakerException");
        } catch (CircuitBreakerException cbe) {
        }
    }
}
