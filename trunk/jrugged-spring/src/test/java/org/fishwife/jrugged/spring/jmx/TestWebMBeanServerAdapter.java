/* TestWebMBeanServerAdapter.java
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
package org.fishwife.jrugged.spring.jmx;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestWebMBeanServerAdapter {

    private MBeanServer mockMbeanServer;
    private MBeanStringSanitizer mockSanitizer;
    private WebMBeanServerAdapter webMBeanServerAdapter;

    @Before
    public void setUp() {
        mockMbeanServer = createMock(MBeanServer.class);
        mockSanitizer = createMock(MBeanStringSanitizer.class);
        webMBeanServerAdapter = new WebMBeanServerAdapter(mockMbeanServer);
        ReflectionTestUtils.setField(webMBeanServerAdapter, "mBeanServer", mockMbeanServer);
        ReflectionTestUtils.setField(webMBeanServerAdapter, "sanitizer", mockSanitizer);
    }

    @Test
    public void testGetMBeanNames() throws Exception {
        String name1 = "com.test:type=objectName,value=1";
        String name2 = "com.test:type=objectName,value=2";
        ObjectName objectName1 = new ObjectName(name1);
        ObjectInstance object1 = createMock(ObjectInstance.class);
        expect(object1.getObjectName()).andReturn(objectName1);

        ObjectName objectName2 = new ObjectName(name2);
        ObjectInstance object2 = createMock(ObjectInstance.class);
        expect(object2.getObjectName()).andReturn(objectName2);

        Set<ObjectInstance> objectInstanceList = new HashSet<ObjectInstance>();
        objectInstanceList.add(object1);
        objectInstanceList.add(object2);
        expect(mockMbeanServer.queryMBeans(null, null)).andReturn(objectInstanceList);

        expect(mockSanitizer.sanitizeObjectName(name1)).andReturn(name1);
        expect(mockSanitizer.sanitizeObjectName(name2)).andReturn(name2);

        replay(mockMbeanServer, mockSanitizer, object1, object2);

        Set<String> resultSet = webMBeanServerAdapter.getMBeanNames();

        assertEquals(2, resultSet.size());
        assertTrue(resultSet.contains(name1));
        assertTrue(resultSet.contains(name2));
        verify(mockMbeanServer);
        verify(mockSanitizer);
        verify(object1);
        verify(object2);
    }
}
