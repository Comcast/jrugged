/* TestRetrier.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestServiceRetrier {

    private class DummyCallable implements Callable<String> {

        private String _message;
        private int _count = 0;
        private int _failCount = 5;
        private Exception _exception;

        public DummyCallable (String message, int failCount) {
            this(message, failCount, new Exception("FAIL!"));
        }

        public DummyCallable (String message, int failCount, Exception exception) {
            _message = message;
            _failCount = failCount;
            _exception = exception;
        }

        public String call() throws Exception {

            _count++;
            if (_count < _failCount)
                throw _exception;

            return _message;
        }

        public int getCount() {
            return _count;
        }
    }

    private class DummyRunnable implements Runnable {

        private int _count = 0;
        private int _failCount = 5;

        public DummyRunnable (int failCount) {
            _failCount = failCount;
        }

        public void run(){

            _count++;
            if (_count < _failCount)
                throw new RuntimeException("FAIL! " + _count);
        }

        public int getCount() {
            return _count;
        }
    }

    class CaptureSleepServiceRetrier extends ServiceRetrier {

        private int _sleepCallCount = 0;
        private List<Long> _capturedSleepValues = new ArrayList<Long>();

        @Override
        protected void sleep(long millis) {
            _sleepCallCount++;
            _capturedSleepValues.add(millis);
        }

        public int getSleepCallCount() {
            return _sleepCallCount;
        }

        public List<Long> getCapturedSleepValues() {
            return _capturedSleepValues;
        }
    }

    @Test
    public void testInvokeSucceedsWithNoRetries() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 5);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);

        String result = retrier.invoke(foo);
        assertEquals(5, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testInvokeSucceedsWithRetries() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 5);

        ServiceRetrier retrier = new ServiceRetrier(100, 5);
        retrier.setDoubleDelay(false);

        String result = retrier.invoke(foo);
        assertEquals(5, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testInvokeSucceedsWithRetriesAndEmptyRetryOn() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 5);

        ServiceRetrier retrier = new ServiceRetrier(100, 5);

        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] retryOn = new Class[0];
        retrier.setRetryOn(retryOn);

        String result = retrier.invoke(foo);
        assertEquals(5, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testInvokeSucceedsWithRetriesAndSpecificExceptionInRetryOn() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 5, new IOException());

        ServiceRetrier retrier = new ServiceRetrier(100, 5);

        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] retryOn = new Class[] { IllegalArgumentException.class, IOException.class };
        retrier.setRetryOn(retryOn);

        String result = retrier.invoke(foo);
        assertEquals(5, foo.getCount());
        assertEquals("Foo!", result);
    }


    @Test
    public void testConstructorWithNoArgs() {
        ServiceRetrier serviceRetrier = new ServiceRetrier();
        Assert.assertEquals(1000, serviceRetrier.getDelay());
        Assert.assertEquals(10, serviceRetrier.getMaxTries());
        assertFalse(serviceRetrier.isDoubleDelay());
        assertFalse(serviceRetrier.isThrowCauseException());
        Assert.assertEquals(null, serviceRetrier.getRetryOn());
    }

    @Test
    public void testConstructorWithDelayAndMaxTries() throws Exception {
        ServiceRetrier retrier = new ServiceRetrier(100, 5);
        assertEquals(5, retrier.getMaxTries());
        assertEquals(100, retrier.getDelay());
    }

    @Test
    public void testConfigurationWithAll() throws Exception {
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[0];

        ServiceRetrier retrier = new ServiceRetrier(100, 5, true, true, retryOn);
        assertEquals(5, retrier.getMaxTries());
        assertEquals(100, retrier.getDelay());
        assertTrue(retrier.isDoubleDelay());
        assertTrue(retrier.isThrowCauseException());
        assertSame(retryOn, retrier.getRetryOn());
    }

    @Test
    public void testInvokeSucceedRunnable() throws Exception {

        DummyRunnable foo = new DummyRunnable(5);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);

        retrier.invoke(foo);
        assertEquals(5, foo.getCount());
    }

    @Test
    public void testInvokeSucceedRunnableResult() throws Exception {

        DummyRunnable foo = new DummyRunnable(5);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);

        String result = "Foo!";
        retrier.invoke(foo, result);
        assertEquals(5, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testDoubleDelay() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 5);

        CaptureSleepServiceRetrier retrier = new CaptureSleepServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(true);

        retrier.invoke(foo);

        assertTrue(retrier.isDoubleDelay());
        assertEquals(4, retrier.getSleepCallCount());
        assertEquals(100L, (long)retrier.getCapturedSleepValues().get(0));
        assertEquals(200L, (long)retrier.getCapturedSleepValues().get(1));
        assertEquals(400L, (long)retrier.getCapturedSleepValues().get(2));
        assertEquals(800L, (long)retrier.getCapturedSleepValues().get(3));
    }

    @Test
    public void testInvokeFail() {

        DummyCallable foo = new DummyCallable("Foo!", 4);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(3);
        retrier.setDoubleDelay(false);

        try {
            retrier.invoke(foo);
            fail("Should have thrown exception");
        } catch (Exception ex) {
            // pass
        }
    }

    @Test
    public void testInvokeFailsWithUnexpectedException() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 2);
        ServiceRetrier retrier = new ServiceRetrier(100, 5);

        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] retryOn = new Class[] { IOException.class };
        retrier.setRetryOn(retryOn);

        try {
            retrier.invoke(foo);
            fail("Should have thrown exception");
        } catch (Exception ex) {
            // pass
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMaxTriesLessThanOneThrowsException() throws Exception {
        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setMaxTries(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDelayLessThanZeroThrowsException() throws Exception {
        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(-1);
    }
}
