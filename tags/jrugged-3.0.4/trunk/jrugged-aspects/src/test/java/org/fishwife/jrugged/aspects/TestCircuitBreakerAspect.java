/* Copyright 2009-2012 Comcast Interactive Media, LLC.

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
import org.fishwife.jrugged.CircuitBreakerException;
import org.fishwife.jrugged.CircuitBreakerFactory;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestCircuitBreakerAspect {

    private CircuitBreakerAspect aspect;

    CircuitBreaker mockAnnotation;

    Signature mockSignature;

    private static final String TEST_CIRCUIT_BREAKER = "TestCircuitBreaker";

    @Before
    public void setUp() {
        aspect = new CircuitBreakerAspect();

        mockAnnotation = createMock(CircuitBreaker.class);
        mockSignature = createMock(Signature.class);

        expect(mockSignature.getName()).andReturn("Signature").anyTimes();
        expect(mockAnnotation.name()).andReturn(TEST_CIRCUIT_BREAKER).anyTimes();
        expect(mockAnnotation.limit()).andReturn(5).anyTimes();
        expect(mockAnnotation.resetMillis()).andReturn(30000L).anyTimes();
        expect(mockAnnotation.windowMillis()).andReturn(10000L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] ignores = new Class[0];
        expect(mockAnnotation.ignore()).andReturn(ignores);

        replay(mockAnnotation);
        replay(mockSignature);
    }

    @Test
    public void testMonitor() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 2);
        expect(mockPjp.proceed()).andReturn(null).times(2);
        replay(mockPjp);

        // Test monitor without pre-existing circuit breaker.
        aspect.monitor(mockPjp, mockAnnotation);

        // Test monitor with pre-existing circuit breaker.
        aspect.monitor(mockPjp, mockAnnotation);

        String otherName = "OtherMonitor";
        ProceedingJoinPoint otherMockPjp = createPjpMock(mockSignature, 1);
        expect(otherMockPjp.proceed()).andReturn(null).times(1);
        replay(otherMockPjp);

        CircuitBreaker otherMockAnnotation = createMock(CircuitBreaker.class);
        expect(otherMockAnnotation.name()).andReturn(otherName).anyTimes();
        expect(otherMockAnnotation.limit()).andReturn(5).anyTimes();
        expect(otherMockAnnotation.resetMillis()).andReturn(30000L).anyTimes();
        expect(otherMockAnnotation.windowMillis()).andReturn(10000L).anyTimes();
        @SuppressWarnings("unchecked")
        Class<Throwable>[] ignores = new Class[0];
        expect(otherMockAnnotation.ignore()).andReturn(ignores);
        replay(otherMockAnnotation);

        // Test monitor with another circuit breaker.
        aspect.monitor(otherMockPjp, otherMockAnnotation);
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);
        verifyBreakerExists(otherName);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        verify(otherMockPjp);
        verify(otherMockAnnotation);
    }

    @Test
    public void testSetCircuitBreakerFactory() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andReturn(null);
        replay(mockPjp);

        CircuitBreakerFactory factory = new CircuitBreakerFactory();
        aspect.setCircuitBreakerFactory(factory);

        aspect.monitor(mockPjp, mockAnnotation);

        assertSame(factory, aspect.getCircuitBreakerFactory());
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testMonitorWithError() throws Throwable {
        Error e = new Error();
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(e);
        replay(mockPjp);

        callMonitorCatchThrowable(mockPjp, e);
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testMonitorWithRunTimeException() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(new Throwable());
        replay(mockPjp);

        callMonitorCatchThrowable(mockPjp, new RuntimeException());
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testMonitorWithException() throws Throwable {
        Exception e = new Exception();
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(e);
        replay(mockPjp);

        callMonitorCatchThrowable(mockPjp, e);
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testGetCircuitBreakerFactory() throws Throwable {

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andReturn(null);
        replay(mockPjp);

        aspect.monitor(mockPjp, mockAnnotation);
        CircuitBreakerFactory circuitBreakerFactory =
                aspect.getCircuitBreakerFactory();

        assertNotNull(circuitBreakerFactory);
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test
    public void testTripBreaker() throws Throwable {
        int pjpCallCount = 7;
        int callCount = pjpCallCount - 1;

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, pjpCallCount);

        expect(mockPjp.proceed()).andThrow(new Exception()).times(callCount);
        replay(mockPjp);

        Exception e = new Exception();

        for (int i = 0; i < callCount; i++) {
            callMonitorCatchThrowable(mockPjp, e);
        }

        CircuitBreakerException cbe = new CircuitBreakerException();
        callMonitorCatchThrowable(mockPjp, cbe);
        verifyBreakerExists(TEST_CIRCUIT_BREAKER);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    private static ProceedingJoinPoint createPjpMock(Signature mockSignature, int times) {
        ProceedingJoinPoint mockPjp = createMock(ProceedingJoinPoint.class);
        expect(mockPjp.getTarget()).andReturn("Target").times(times);
        expect(mockPjp.getSignature()).andReturn(mockSignature).times(times);
        return mockPjp;
    }

    private void callMonitorCatchThrowable(
            ProceedingJoinPoint pjp, Throwable expected) {
        try {
            aspect.monitor(pjp, mockAnnotation);
        }
        catch (Throwable thrown) {
            assertEquals(expected.getClass(), thrown.getClass());
        }
    }
    private void verifyBreakerExists(String name) {
        assertNotNull(aspect.getCircuitBreakerFactory().findCircuitBreaker(name));
    }
}
