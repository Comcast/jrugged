/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged.spring;

import org.easymock.EasyMock;
import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerConfig;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.concurrent.ConcurrentHashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

public class TestCircuitBreakerBeanFactory {

    private CircuitBreakerBeanFactory factory;
    private CircuitBreakerConfig config;

    MBeanExporter mockMBeanExporter;

    @Before
    public void setUp() {
        factory = new CircuitBreakerBeanFactory();
        config = new CircuitBreakerConfig(10000L, new DefaultFailureInterpreter(5, 30000L));
        mockMBeanExporter = createMock(MBeanExporter.class);
        mockMBeanExporter.registerManagedResource(EasyMock.<Object>anyObject(), EasyMock.<ObjectName>anyObject());
        replay(mockMBeanExporter);
    }

    @Test
    public void testCreateCircuitBreaker() {
        CircuitBreaker createdBreaker = factory.createCircuitBreaker("testCreate", config);
        assertNotNull(createdBreaker);
    }

    @Test
    public void testCreateDuplicateCircuitBreaker() {
        String name = "testCreate";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(name, config);
        CircuitBreaker secondBreaker = factory.createCircuitBreaker(name, config);

        assertSame(createdBreaker, secondBreaker);
    }

    @Test
    public void testFindCircuitBreakerBean() {
        String breakerName = "testFind";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(breakerName, config);
        CircuitBreakerBean foundBreaker = factory.findCircuitBreakerBean(breakerName);
        assertNotNull(foundBreaker);
        assertEquals(createdBreaker, foundBreaker);
    }

    @Test
    public void testFindInvalidCircuitBreakerBean() {
        String breakerName = "testFindInvalid";

        // Create a map with an invalid CircuitBreaker (non-bean) in it, and jam it in.
        ConcurrentHashMap<String, CircuitBreaker> invalidMap = new ConcurrentHashMap<String, CircuitBreaker>();
        invalidMap.put(breakerName, new CircuitBreaker());
        ReflectionTestUtils.setField(factory, "circuitBreakerMap", invalidMap);

        // Try to find it.
        CircuitBreakerBean foundBreaker = factory.findCircuitBreakerBean(breakerName);
        assertNull(foundBreaker);
    }

    @Test
    public void testCreatePerformanceMonitorObjectName() throws MalformedObjectNameException, NullPointerException {
        mockMBeanExporter = createMock(MBeanExporter.class);
        ObjectName objectName = new ObjectName(
                            "org.fishwife.jrugged.spring:type=" +
                                    "CircuitBreakerBean,name=testCreate");
        mockMBeanExporter.registerManagedResource(EasyMock.<Object>anyObject(), EasyMock.eq(objectName));
        replay(mockMBeanExporter);

        factory.setMBeanExportOperations(mockMBeanExporter);
        factory.createCircuitBreaker("testCreate", config);
        EasyMock.verify(mockMBeanExporter);
    }

    @Test
    public void testBreakerWithoutMBeanExporter() {
        factory.setMBeanExportOperations(null);
        CircuitBreaker createdBreaker = factory.createCircuitBreaker("testCreateWithoutMBeanExporter", config);
        assertNotNull(createdBreaker);
    }

    @Test
    public void testBreakerWithMBeanExporter() {
        factory.setMBeanExportOperations(mockMBeanExporter);
        CircuitBreaker createdBreaker = factory.createCircuitBreaker("testCreateWithoutMBeanExporter", config);
        assertNotNull(createdBreaker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBreakerWithInvalidName() {
        factory.setMBeanExportOperations(mockMBeanExporter);
        factory.createCircuitBreaker("=\"", config);
    }

    //---------
    // The following tests depend on org.fishwife.jrugged.spring.testonly

    @Test
    public void testFactoryFindsCircuitBreakers() {
        factory.setPackageScanBase("org.fishwife.jrugged.spring.testonly");
        factory.buildAnnotatedCircuitBreakers();

        assertNotNull(factory.findCircuitBreaker("breakerA"));
        assertNotNull(factory.findCircuitBreaker("breakerB"));
    }

    @Test
    public void testWhenPackageScanNotProvidedAnnotationsNotLoaded() {
        factory.buildAnnotatedCircuitBreakers();
        assertNull(factory.findCircuitBreaker("breakerA"));
        assertNull(factory.findCircuitBreaker("breakerB"));
    }

}
