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
package org.fishwife.jrugged;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

public class TestPerformanceMonitorFactory {

    private PerformanceMonitorFactory factory;

    @Before
    public void setUp() {
        factory = new PerformanceMonitorFactory();
    }

    @Test
    public void testCreatePerformanceMonitor() {
        PerformanceMonitor createdMonitor = factory.createPerformanceMonitor("testCreate");
        assertNotNull(createdMonitor);
    }

    @Test
    public void testCreateDuplicatePerformanceMonitor() {
        String name = "testCreate";
        PerformanceMonitor createdMonitor = factory.createPerformanceMonitor(name);
        PerformanceMonitor secondMonitor = factory.createPerformanceMonitor(name);
        assertSame(createdMonitor, secondMonitor);
    }

    @Test
    public void testFindPerformanceMonitor() {
        String monitorName = "testFind";
        PerformanceMonitor createdMonitor = factory.createPerformanceMonitor(monitorName);
        PerformanceMonitor foundMonitor = factory.findPerformanceMonitor(monitorName);
        assertNotNull(foundMonitor);
        assertEquals(createdMonitor, foundMonitor);
    }

    @Test
    public void testFindNonExistentPerformanceMonitor() {
        PerformanceMonitor foundMonitor = factory.findPerformanceMonitor("testNonExistent");
        assertNull(foundMonitor);
    }

    @Test
    public void testGetPerformanceMonitorNames() {
        Set<String> testSet = new HashSet<String>();
        testSet.add("one");
        testSet.add("two");
        testSet.add("three");
        testSet.add("four");

        factory.createPerformanceMonitor("one");
        factory.createPerformanceMonitor("two");
        factory.createPerformanceMonitor("three");
        factory.createPerformanceMonitor("four");
        Set<String> monitorNames = factory.getPerformanceMonitorNames();
        assertEquals(testSet, monitorNames);
    }
}
