/* Copyright 2009-2011 Comcast Interactive Media, LLC.

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
import org.fishwife.jrugged.PerformanceMonitorFactory;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class PerformanceMonitorAspectTest {

    private PerformanceMonitorAspect aspect;

    PerformanceMonitor mockAnnotation;

    Signature mockSignature;

    private static final String TEST_MONITOR = "TestMonitor";

    @Before
    public void setUp() {
        aspect = new PerformanceMonitorAspect();

        mockSignature = createMock(Signature.class);
        mockAnnotation = createMock(PerformanceMonitor.class);

        expect(mockSignature.getName()).andReturn("Signature").anyTimes();
        expect(mockAnnotation.value()).andReturn(TEST_MONITOR).anyTimes();

        replay(mockAnnotation);
        replay(mockSignature);
    }

    private static ProceedingJoinPoint createPjpMock(Signature mockSignature,
                                                     int times) {
        ProceedingJoinPoint mockPjp = createMock(ProceedingJoinPoint.class);
        expect(mockPjp.getTarget()).andReturn("Target").times(times);
        expect(mockPjp.getSignature()).andReturn(mockSignature).times(times);
        return mockPjp;
    }

    @Test
    public void testMonitor() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 2);
        expect(mockPjp.proceed()).andReturn(null).times(2);
        replay(mockPjp);

        // Test monitor without pre-existing perf monitor.
        aspect.monitor(mockPjp, mockAnnotation);

        // Test monitor with pre-existing perf monitor.
        aspect.monitor(mockPjp, mockAnnotation);

        String otherMonitor = "OtherMonitor";

        ProceedingJoinPoint otherMockPjp = createPjpMock(mockSignature, 1);
        expect(otherMockPjp.proceed()).andReturn(null).times(1);
        replay(otherMockPjp);

        PerformanceMonitor otherMockAnnotation = createMock(PerformanceMonitor.class);
        expect(otherMockAnnotation.value()).andReturn(otherMonitor);
        replay(otherMockAnnotation);

        // Test monitor with another perf monitor.
        aspect.monitor(otherMockPjp, otherMockAnnotation);

        verifyMonitor(TEST_MONITOR, 2, 0);
        verifyMonitor(otherMonitor, 1, 0);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
        verify(otherMockPjp);
        verify(otherMockAnnotation);
    }

    @Test
    public void testSetPerformanceMonitorFactory() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andReturn(null);
        replay(mockPjp);

        PerformanceMonitorFactory factory = new PerformanceMonitorFactory();
        aspect.setPerformanceMonitorFactory(factory);

        aspect.monitor(mockPjp, mockAnnotation);

        assertSame(factory, aspect.getPerformanceMonitorFactory());
        verifyMonitor(TEST_MONITOR, 1, 0);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test(expected = Throwable.class)
    public void testMonitorWithThrowable() throws Throwable {

        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(new Throwable());
        replay(mockPjp);

        aspect.monitor(mockPjp, mockAnnotation);
        verifyMonitor(TEST_MONITOR, 0, 1);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    @Test(expected = Exception.class)
    public void testMonitorWithException() throws Throwable {
        ProceedingJoinPoint mockPjp = createPjpMock(mockSignature, 1);
        expect(mockPjp.proceed()).andThrow(new Exception());
        replay(mockPjp);

        aspect.monitor(mockPjp, mockAnnotation);
        verifyMonitor(TEST_MONITOR, 0, 1);

        verify(mockPjp);
        verify(mockAnnotation);
        verify(mockSignature);
    }

    private void verifyMonitor(String name, int successCount, int failureCount) {
        org.fishwife.jrugged.PerformanceMonitor monitor =
                aspect.getPerformanceMonitorFactory().findPerformanceMonitor(name);
        assertNotNull(monitor);
        assertEquals(successCount, monitor.getSuccessCount());
        assertEquals(failureCount, monitor.getFailureCount());
    }
}
