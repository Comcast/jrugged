package org.fishwife.jrugged;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.Test;


public class TestServiceWrapperChain {

    private ServiceWrapperChain impl;
    
    @Test
    public void callableJustGetsInvokedWithNoWrappers() throws Exception {
        impl = new ServiceWrapperChain(new ArrayList<ServiceWrapper>());
        final Object out = new Object();
        Callable<Object> c = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
        assertSame(out, impl.invoke(c));
    }
    
    @Test
    public void runnableJustGetsRunWithNoWrappers() throws Exception {
        Runnable r = createMock(Runnable.class);
        impl = new ServiceWrapperChain(new ArrayList<ServiceWrapper>());
        r.run();
        replay(r);
        impl.invoke(r);
        verify(r);
    }
    
    @Test
    public void runnableWithResultJustGetsRunWithNoWrappers() throws Exception {
        Runnable r = createMock(Runnable.class);
        impl = new ServiceWrapperChain(new ArrayList<ServiceWrapper>());
        final Object out = new Object();
        r.run();
        replay(r);
        Object result = impl.invoke(r, out);
        verify(r);        
        assertSame(out, result);
    }
    
    @Test
    public void callableWithOneWrapperRunsThroughWrapper() throws Exception {
        final Object out = new Object();
        Callable<Object> c = new Callable<Object>() {
            public Object call() throws Exception {
                return null;
            }
        };
        ServiceWrapper wrapper = createMock(ServiceWrapper.class);
        impl = new ServiceWrapperChain(Arrays.asList(wrapper));

        expect(wrapper.invoke(c)).andReturn(out);
        replay(wrapper);
        Object result = impl.invoke(c);
        verify(wrapper);
        assertSame(out, result);
    }

    @Test
    public void runnableWithOneWrapperRunsThroughWrapper() throws Exception {
        Runnable r = createMock(Runnable.class);
        ServiceWrapper wrapper = createMock(ServiceWrapper.class);
        impl = new ServiceWrapperChain(Arrays.asList(wrapper));

        wrapper.invoke(r);
        replay(wrapper);
        replay(r);
        impl.invoke(r);
        verify(wrapper);
        verify(r);
    }

    @Test
    public void runnableWithReturnWithOneWrapperRunsThroughWrapper() throws Exception {
        final Object out = new Object();
        Runnable r = createMock(Runnable.class);
        ServiceWrapper wrapper = createMock(ServiceWrapper.class);
        impl = new ServiceWrapperChain(Arrays.asList(wrapper));

        wrapper.invoke(r);
        replay(wrapper);
        replay(r);
        Object result = impl.invoke(r, out);
        verify(wrapper);
        verify(r);
        assertSame(out, result);
    }
    
    @Test
    public void callableWithTwoWrappersRunsThroughFirstWrapperFirst() throws Exception {
        final Object out1 = new Object();
        final Object out2 = new Object();
        Callable<Object> c = new NullCallable();
        ServiceWrapper wrapper1 = new NullWrapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T invoke(Callable<T> c) throws Exception {
                return (T)out1;
            }
        };
        ServiceWrapper wrapper2 = new NullWrapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T invoke(Callable<T> c) throws Exception {
                return (T)out2;
            }
        };
        impl = new ServiceWrapperChain(Arrays.asList(wrapper1, wrapper2));
        assertSame(out1, impl.invoke(c));
    }
    
    @Test
    public void runnableWithTwoWrappersRunsThroughFirstWrapperFirst() throws Exception {
        Runnable r = new NullRunnable();
        final Flag f1 = new Flag();
        final Flag f2 = new Flag();
        ServiceWrapper wrapper1 = new NullWrapper() {
            @Override
            public void invoke(Runnable r) throws Exception {
                f1.set = true;
            }
        };
        ServiceWrapper wrapper2 = new NullWrapper() {
            @Override
            public void invoke(Runnable r) throws Exception {
                f2.set = false;
            }
        };
        impl = new ServiceWrapperChain(Arrays.asList(wrapper1, wrapper2));
        impl.invoke(r);
        assertTrue(f1.set);
        assertFalse(f2.set);
    }
    
    @Test
    public void runnableWithReturnWithTwoWrappersRunsThroughFirstWrapperFirst() throws Exception {
        final Object out = new Object();
        Runnable r = new NullRunnable();
        final Flag f1 = new Flag();
        final Flag f2 = new Flag();
        ServiceWrapper wrapper1 = new NullWrapper() {
            @Override
            public void invoke(Runnable r) throws Exception {
                f1.set = true;
            }
        };
        ServiceWrapper wrapper2 = new NullWrapper() {
            @Override
            public void invoke(Runnable r) throws Exception {
                f2.set = false;
            }
        };
        impl = new ServiceWrapperChain(Arrays.asList(wrapper1, wrapper2));
        Object result = impl.invoke(r, out);
        assertTrue(f1.set);
        assertFalse(f2.set);
        assertSame(out, result);
    }
    
    @Test
    public void callableWithTwoNullWrappersThreadsProperly() throws Exception {
        final Object out = new Object();
        Callable<Object> c = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
        NullWrapper wrapper1 = new NullWrapper();
        NullWrapper wrapper2 = new NullWrapper();
        impl = new ServiceWrapperChain(Arrays.asList((ServiceWrapper)wrapper1, (ServiceWrapper)wrapper2));
        Object result = impl.invoke(c);
        assertTrue(wrapper1.invokedCallable);
        assertTrue(wrapper2.invokedCallable);
        assertSame(out, result);
    }
    
    @Test
    public void runnableWithTwoNullWrappersThreadsProperly() throws Exception {
        final Flag f = new Flag();
        Runnable r = new Runnable() {
            public void run() {
                f.set = true;
            }
        };
        NullWrapper wrapper1 = new NullWrapper();
        NullWrapper wrapper2 = new NullWrapper();
        impl = new ServiceWrapperChain(Arrays.asList((ServiceWrapper)wrapper1, (ServiceWrapper)wrapper2));
        impl.invoke(r);
        assertTrue(wrapper1.invokedRunnable);
        assertTrue(wrapper2.invokedRunnable);
        assertTrue(f.set);
    }

    @Test
    public void runnableWithReturnWithTwoNullWrappersThreadsProperly() throws Exception {
        final Object out = new Object();
        final Flag f = new Flag();
        Runnable r = new Runnable() {
            public void run() {
                f.set = true;
            }
        };
        NullWrapper wrapper1 = new NullWrapper();
        NullWrapper wrapper2 = new NullWrapper();
        impl = new ServiceWrapperChain(Arrays.asList((ServiceWrapper)wrapper1, (ServiceWrapper)wrapper2));
        Object result = impl.invoke(r, out);
        assertTrue(wrapper1.invokedRunnable);
        assertTrue(wrapper2.invokedRunnable);
        assertTrue(f.set);
        assertSame(out, result);
    }
    
    private static class Flag {
        public boolean set = false;
    }
    
    private static class NullRunnable implements Runnable {
        public void run() { }
    }
    
    private static class NullCallable implements Callable<Object> {
        public Object call() throws Exception {
            return null;
        }
    }
    
    private static class NullWrapper implements ServiceWrapper {
        public boolean invokedCallable;
        public boolean invokedRunnable;
        
        public <T> T invoke(Callable<T> c) throws Exception {
            invokedCallable = true;
            return c.call();
        }

        public void invoke(Runnable r) throws Exception {
            invokedRunnable = true;
            r.run();
        }

        public <T> T invoke(Runnable r, T result) throws Exception {
            return null;
        }        
    }
}
