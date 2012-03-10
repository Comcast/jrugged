package org.fishwife.jrugged;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class TestRequestCounter {
    
    private RequestCounter impl;
    
    @Before
    public void setUp() {
        impl = new RequestCounter();
    }
    
    @Test
    public void testCountersInitiallyZero() {
        assertArrayEquals(new long[] {0L, 0L, 0L}, impl.sample());
    }
    
    @Test
    public void testSucceedingCallable() throws Exception {
        final Object o = new Object();
        
        Object result = impl.invoke(new Callable<Object>() {
            public Object call() throws Exception {
                return o;
            }
        });
        
        assertSame(o, result);
        assertArrayEquals(new long[] {1L, 1L, 0L}, impl.sample());
    }
    
    @Test
    public void testFailingCallable() throws Exception {
        final Exception ex = new Exception();
        
        try {
            impl.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    throw ex;
                }
            });
            fail("should have thrown exception");
        } catch (Exception e) {
            assertSame(ex, e);
        }
        
        assertArrayEquals(new long[] {1L, 0L, 1L}, impl.sample());
    }
    
    @Test
    public void testSucceedingRunnable() throws Exception {
        impl.invoke(new Runnable() {
            public void run() {
                // no-op
            }
        });
        
        assertArrayEquals(new long[] {1L, 1L, 0L}, impl.sample());
    }
    
    @Test
    public void testFailingRunnable() {
        final RuntimeException rtex = new RuntimeException();
        
        try {
            impl.invoke(new Runnable() {
                public void run() {
                    throw rtex;
                }
            });
            fail("should have thrown exception");
        } catch (Exception e) {
            assertSame(rtex, e);
        }
        
        assertArrayEquals(new long[] {1L, 0L, 1L}, impl.sample());
    }
    
    @Test
    public void testSucceedingRunnableWithResult() throws Exception {
        final Object o = new Object();
        
        Object result = impl.invoke(new Runnable() {
            public void run() {
                // no-op
            }
        }, o);
        
        assertSame(o, result);
        assertArrayEquals(new long[] {1L, 1L, 0L}, impl.sample());
    }
    
    @Test
    public void testFailingRunnableWithResult()  {
        final Object o = new Object();
        final RuntimeException rtex = new RuntimeException();
        
        try {
            impl.invoke(new Runnable() {
                public void run() {
                    throw rtex;
                }
            }, o);
            fail("should have thrown exception");
        } catch (Exception e) {
            assertSame(rtex, e);
        }
        
        assertArrayEquals(new long[] {1L, 0L, 1L}, impl.sample());
    }
}
