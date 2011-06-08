package org.fishwife.jrugged.spring;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.fishwife.jrugged.CircuitBreakerException;
import org.junit.Before;
import org.junit.Test;


public class TestCircuitBreakerBean {

    private CircuitBreakerBean impl;
    private final Object out = new Object();
    private Callable<Object> call;
    
    @Before
    public void setUp() {
        impl = new CircuitBreakerBean();
        call = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
    }
    
    @Test
    public void startsEnabled() throws Exception {
        assertSame(out, impl.invoke(call));
    }
    
    @Test
    public void isenabledIfConfiguredAsNotDisabled() throws Exception {
        impl.setDisabled(false);
        impl.afterPropertiesSet();
        assertSame(out, impl.invoke(call));
    }
    
    @Test
    public void canBeDisabled() throws Exception {
        impl.setDisabled(true);
        impl.afterPropertiesSet();
        try {
            impl.invoke(call);
            fail("Should have thrown CircuitBreakerException");
        } catch (CircuitBreakerException cbe) {
        }
    }
}
