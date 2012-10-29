/* TestRetrier.java
 * 
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestRetrier {
    
    private class DummyCallable implements Callable<String> {
        
        private String _message;
        private int _count = 0;
        private int _failCount = 5;
        
        public DummyCallable (String message, int failCount) {
            _message = message;
            _failCount = failCount;
        }
        
        public String call() throws Exception {
            
            _count++;
            if (_count < _failCount)
                throw new Exception("FAIL! " + _count);
            
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

    @Test
    public void testInvokeSucceedCallable() throws Exception {
        
        DummyCallable foo = new DummyCallable("Foo!", 4);
        
        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);
        
        String result = retrier.invoke(foo);
        assertEquals(4, foo.getCount());
        assertEquals("Foo!", result);
    }
    
    @Test
    public void testInvokeSucceedCallableWithConstructor() throws Exception {

        DummyCallable foo = new DummyCallable("Foo!", 4);

        ServiceRetrier retrier = new ServiceRetrier(100, 5);
        retrier.setDoubleDelay(false);

        String result = retrier.invoke(foo);
        assertEquals(4, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testConfiguration() throws Exception {
        ServiceRetrier retrier = new ServiceRetrier(100, 5);
        assertEquals(5, retrier.getMaxTries());
        assertEquals(100, retrier.getDelay());
    }

    @Test
    public void testInvokeSucceedRunnable() throws Exception {

        DummyRunnable foo = new DummyRunnable(4);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);

        retrier.invoke(foo);
        assertEquals(4, foo.getCount());
    }

    @Test
    public void testInvokeSucceedRunnableResult() throws Exception {

        DummyRunnable foo = new DummyRunnable(4);

        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(false);

        String result = "Foo!";
        retrier.invoke(foo, result);
        assertEquals(4, foo.getCount());
        assertEquals("Foo!", result);
    }

    @Test
    public void testDoubleDelay() throws Exception {
        
        DummyCallable foo = new DummyCallable("Foo!", 4);
        
        ServiceRetrier retrier = new ServiceRetrier();
        retrier.setDelay(100);
        retrier.setMaxTries(5);
        retrier.setDoubleDelay(true);
        
        long startTime = System.currentTimeMillis();
        retrier.invoke(foo);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (duration < (100 + 200 + 400))
            fail("ServiceRetrier did not double delays");
        
        if (duration > (100 + 200 + 400 + 800))
            fail("ServiceRetrier delayed too long");

        assertTrue(retrier.isDoubleDelay());
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
