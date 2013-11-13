/* Copyright 2009-2013 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestRetryableAspect {

    class MockRetryableAspect extends RetryableAspect {

        private int delayCallCount = 0;
        private long capturedDelay;

        protected void delay(long millis) {
            delayCallCount++;
            capturedDelay = millis;
        }

        public int getDelayCallCount() {
            return delayCallCount;
        }

        public long getCapturedDelay() {
            return capturedDelay;
        }
    }

    private MockRetryableAspect aspect;

    private Retryable mockAnnotation;

    private Signature mockSignature;


    @Before
    public void setUp() {
        aspect = new MockRetryableAspect();

        mockAnnotation = createMock(Retryable.class);

        mockSignature = createMock(Signature.class);
        expect(mockSignature.getName()).andReturn("Signature").anyTimes();
        replay(mockSignature);
    }

    @Test
    public void testCall_WithNoRetries() throws Throwable {
        expect(mockAnnotation.maxRetries()).andReturn(0).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(0L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[0];
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        replay(mockAnnotation);

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andReturn(null).times(1);
        replay(mockPjp);

        aspect.call(mockPjp, mockAnnotation);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        assertEquals(0, aspect.getDelayCallCount());
    }

    @Test
    public void testCall_WithRetries_WithNoExceptions() throws Throwable {
        expect(mockAnnotation.maxRetries()).andReturn(2).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(123L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[0];
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        replay(mockAnnotation);

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        // Only called once since there are no exceptions.
        expect(mockPjp.proceed()).andReturn(null).times(1);
        replay(mockPjp);

        aspect.call(mockPjp, mockAnnotation);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        assertEquals(0, aspect.getDelayCallCount());
    }

    @Test
    public void testCallWithRetries_WithExceptions() throws Throwable {
        expect(mockAnnotation.maxRetries()).andReturn(2).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(123L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[0];
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        replay(mockAnnotation);

        Exception e = new Exception();
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 3);

        // Called 3 times due to Exceptions.
        expect(mockPjp.proceed()).andThrow(e).times(3);
        replay(mockPjp);

        callCatchThrowable(mockPjp, e);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        assertEquals(2, aspect.getDelayCallCount());
        assertEquals(123L, aspect.getCapturedDelay());
    }

    @Test
    public void testCall_WithRetries_WithTwoExceptions_AndThenSuccess() throws Throwable {
        expect(mockAnnotation.maxRetries()).andReturn(2).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(123L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[] { Exception.class };
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        replay(mockAnnotation);

        Exception e = new Exception();
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 3);

        // Called 3 times due to Exceptions.
        expect(mockPjp.proceed()).andThrow(e).andThrow(e).andReturn(null);
        replay(mockPjp);

        aspect.call(mockPjp, mockAnnotation);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        assertEquals(2, aspect.getDelayCallCount());
        assertEquals(123L, aspect.getCapturedDelay());
    }

    @Test
    public void testCall_WithUnexpectedThrowable() throws Throwable {
        expect(mockAnnotation.maxRetries()).andReturn(2).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(0L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[] { IOException.class };
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        replay(mockAnnotation);

        Throwable t = new Throwable();
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(t);
        replay(mockPjp);

        callCatchThrowable(mockPjp, t);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        assertEquals(0, aspect.getDelayCallCount());
    }

    private static ProceedingJoinPoint createPjpMock(Signature mockSignature, int times) {
        ProceedingJoinPoint mockPjp = createMock(ProceedingJoinPoint.class);
        // XXX: the following two interactions are for logging, so they may happen
        //      0 or n times, pending logging configuration
        expect(mockPjp.getTarget()).andReturn("Target").times(0, times);
        expect(mockPjp.getSignature()).andReturn(mockSignature).times(0, times);
        return mockPjp;
    }

    private void callCatchThrowable(ProceedingJoinPoint pjp, Throwable expected) {
        try {
            aspect.call(pjp, mockAnnotation);
        }
        catch (Throwable thrown) {
            assertEquals(expected.getClass(), thrown.getClass());
        }
    }
}
