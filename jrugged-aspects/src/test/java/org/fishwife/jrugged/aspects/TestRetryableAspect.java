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

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestRetryableAspect {

    private RetryableAspect aspect;

    private Retryable mockAnnotation;

    private Signature mockSignature;


    @Before
    public void setUp() {
        aspect = new RetryableAspect();

        mockAnnotation = createMock(Retryable.class);
        expect(mockAnnotation.maxTries()).andReturn(1).anyTimes();
        expect(mockAnnotation.retryDelayMillis()).andReturn(0).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] retryOn = new Class[0];
        expect(mockAnnotation.retryOn()).andReturn(retryOn);
        expect(mockAnnotation.doubleDelay()).andReturn(true);
        expect(mockAnnotation.throwCauseException()).andReturn(true);
        replay(mockAnnotation);

        mockSignature = createMock(Signature.class);
        expect(mockSignature.getName()).andReturn("Signature").anyTimes();
        replay(mockSignature);
    }

    @Test
    public void testCall() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature);
        expect(mockPjp.proceed()).andReturn(null).times(1);
        replay(mockPjp);

        aspect.call(mockPjp, mockAnnotation);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testCall_WithException() throws Throwable {
        Exception exception = new Exception();

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature);
        expect(mockPjp.proceed()).andThrow(exception);
        replay(mockPjp);

        callCatchThrowable(mockPjp, exception);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testCall_WithError() throws Throwable {
        Error error = new Error();

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature);
        expect(mockPjp.proceed()).andThrow(error);
        replay(mockPjp);

        callCatchThrowable(mockPjp, error);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testCall_WithThrowable() throws Throwable {
        Throwable throwable = new Throwable();

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature);
        expect(mockPjp.proceed()).andThrow(throwable);
        replay(mockPjp);

        callCatchThrowable(mockPjp, new RuntimeException(throwable));

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    private static ProceedingJoinPoint createPjpMock(Signature mockSignature) {
        ProceedingJoinPoint mockPjp = createMock(ProceedingJoinPoint.class);
        // XXX: the following two interactions are for logging, so they may happen
        //      0 or n times, pending logging configuration
        expect(mockPjp.getTarget()).andReturn("Target").times(0, 1);
        expect(mockPjp.getSignature()).andReturn(mockSignature).times(0, 1);
        return mockPjp;
    }

    private void callCatchThrowable(ProceedingJoinPoint pjp, Throwable expected) {
        try {
            aspect.call(pjp, mockAnnotation);
        }
        catch (Throwable thrown) {
            thrown.printStackTrace();
            assertEquals(expected.getClass(), thrown.getClass());
        }
    }
}
