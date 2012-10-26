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
package org.fishwife.jrugged.spring;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.easymock.EasyMock;
import org.fishwife.jrugged.PerformanceMonitor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.test.util.ReflectionTestUtils;

public class TestPerformanceMonitorBeanFactory {

    private PerformanceMonitorBeanFactory factory;

    private MBeanExporter mockMBeanExporter;

    @Before
    public void setUp() {
        factory = new PerformanceMonitorBeanFactory();
    }

    @Test
    public void testCreateWithInitialPerformanceMonitors() {
        String name1 = "test1";
        String name2 = "test2";
        List<String> nameList = new ArrayList<String>();
        nameList.add(name1);
        nameList.add(name2);

        factory.setInitialPerformanceMonitors(null);
        factory.createInitialPerformanceMonitors();

        factory.setInitialPerformanceMonitors(nameList);
        factory.createInitialPerformanceMonitors();

        assertNotNull(factory.findPerformanceMonitor(name1));
        assertNotNull(factory.findPerformanceMonitor(name2));
    }

    @Test
    public void testCreatePerformanceMonitor() {
        PerformanceMonitor createdMonitor =
          factory.createPerformanceMonitor("testCreate");
        assertNotNull(createdMonitor);
    }

    @Test
    public void testCreatePerformanceMonitorObjectName() throws MalformedObjectNameException, NullPointerException {
        mockMBeanExporter = createMock(MBeanExporter.class);
        ObjectName objectName = new ObjectName(
                            "org.fishwife.jrugged.spring:type=" +
                                    "PerformanceMonitorBean,name=testCreate");
        mockMBeanExporter.registerManagedResource(EasyMock.<Object>anyObject(), EasyMock.eq(objectName));
        replay(mockMBeanExporter);
        
        factory.setMBeanExporter(mockMBeanExporter);
        factory.createPerformanceMonitor("testCreate");
        EasyMock.verify(mockMBeanExporter);
    }

    @Test
    public void testCreateDuplicatePerformanceMonitor() {
        String name = "testCreate";
        PerformanceMonitor createdMonitor =
          factory.createPerformanceMonitor(name);
        PerformanceMonitor secondMonitor =
          factory.createPerformanceMonitor(name);
        assertSame(createdMonitor, secondMonitor);
    }

    @Test
    public void testFindPerformanceMonitorBean() {
        String monitorName = "testFind";
        PerformanceMonitor createdMonitor =
          factory.createPerformanceMonitor(monitorName);
        PerformanceMonitorBean foundMonitor =
          factory.findPerformanceMonitorBean(monitorName);
        assertNotNull(foundMonitor);
        assertEquals(createdMonitor, foundMonitor);
    }

    @Test
    public void testFindInvalidPerformanceMonitorBean() {
        String monitorName = "testFindInvalid";

        // Create a map with an invalid PerformanceMonitor (non-bean) in it,
        // and jam it in.
        Map<String, PerformanceMonitor> invalidMap =
          new HashMap<String, PerformanceMonitor>();
        invalidMap.put(monitorName, new PerformanceMonitor());
        ReflectionTestUtils.setField(
          factory, "performanceMonitorMap", invalidMap);

        // Try to find it.
        PerformanceMonitorBean foundMonitor =
          factory.findPerformanceMonitorBean(monitorName);
        assertNull(foundMonitor);
    }
    @Test
    public void testMonitorWithoutMBeanExporter() {
        factory.setMBeanExporter(null);
        PerformanceMonitor createdMonitor =
                factory.createPerformanceMonitor(
                  "testCreateWithoutMBeanExporter");
        assertNotNull(createdMonitor);
    }

    @Test
    public void testMonitorWithMBeanExporter() {
        mockMBeanExporter = createMock(MBeanExporter.class);
        mockMBeanExporter.registerManagedResource(
          EasyMock.<Object>anyObject(), EasyMock.<ObjectName>anyObject());
        replay(mockMBeanExporter);
        
        factory.setMBeanExporter(mockMBeanExporter);
        PerformanceMonitor createdMonitor =
                factory.createPerformanceMonitor(
                  "testCreateWithoutMBeanExporter");
        assertNotNull(createdMonitor);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMonitorWithInvalidName() {
        mockMBeanExporter = createMock(MBeanExporter.class);
        mockMBeanExporter.registerManagedResource(
          EasyMock.<Object>anyObject(), EasyMock.<ObjectName>anyObject());
        replay(mockMBeanExporter);

        factory.setMBeanExporter(mockMBeanExporter);
        factory.createPerformanceMonitor("=\"");
    }
    

    //---------
    // The following tests depend on org.fishwife.jrugged.spring.testonly
    
    @Test
    public void testFactorySeededWithPackageScanBaseFindsMonitors() {
        factory.setPackageScanBase("org.fishwife.jrugged.spring.testonly");
        factory.createInitialPerformanceMonitors();
        
        assertNotNull(factory.findPerformanceMonitor("monitorA"));
        assertNotNull(factory.findPerformanceMonitor("monitorB"));
    }
    
    // the idea here is that the monitors are created and no exceptions occur
    @Test
    public void testPackageScanStyleAndInitialMonitorStylePlayNice() {
        List<String> initialPerformanceMonitors = new ArrayList<String>();
        initialPerformanceMonitors.add("monitorA");
        
        factory.setPackageScanBase("org.fishwife.jrugged.spring.testonly");
        factory.setInitialPerformanceMonitors(initialPerformanceMonitors);
        factory.createInitialPerformanceMonitors();
        
        assertNotNull(factory.findPerformanceMonitor("monitorA"));
        assertNotNull(factory.findPerformanceMonitor("monitorB"));
    }
}
