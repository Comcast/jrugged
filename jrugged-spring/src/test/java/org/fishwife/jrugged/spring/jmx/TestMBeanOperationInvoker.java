/* TestMBeanOperationInvoker.java
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
package org.fishwife.jrugged.spring.jmx;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestMBeanOperationInvoker {

    private MBeanServer mockMBeanServer;
    private ObjectName mockObjectName;
    private MBeanOperationInfo mockOperationInfo;
    private MBeanValueConverter mockValueConverter;

    private MBeanOperationInvoker invoker;

    class MockMBeanOperationInvoker extends MBeanOperationInvoker {
        MockMBeanOperationInvoker(MBeanServer mBeanServer, ObjectName objectName, MBeanOperationInfo operationInfo) {
            super(mBeanServer, objectName, operationInfo);
        }

        MBeanValueConverter createMBeanValueConverter(Map<String, String[]> parameterMap) {
            return mockValueConverter;
        }
    }

    @Before
    public void setUp() {
        mockMBeanServer = createMock(MBeanServer.class);
        mockObjectName = createMock(ObjectName.class);
        mockOperationInfo = createMock(MBeanOperationInfo.class);
        mockValueConverter = createMock(MBeanValueConverter.class);

        invoker = new MockMBeanOperationInvoker(mockMBeanServer, mockObjectName, mockOperationInfo);
    }

    @Test
    public void testConstructor() {
        assertEquals(mockMBeanServer, ReflectionTestUtils.getField(invoker, "mBeanServer"));
        assertEquals(mockObjectName, ReflectionTestUtils.getField(invoker, "objectName"));
        assertEquals(mockOperationInfo, ReflectionTestUtils.getField(invoker, "operationInfo"));
    }

    @Test
    public void testInvokeOperation() throws JMException {
        MBeanParameterInfo mockParameterInfo1 = createMock(MBeanParameterInfo.class);
        MBeanParameterInfo mockParameterInfo2 = createMock(MBeanParameterInfo.class);
        MBeanParameterInfo[] parameterInfoArray = new MBeanParameterInfo[] { mockParameterInfo1, mockParameterInfo2 };

        expect(mockOperationInfo.getSignature()).andReturn(parameterInfoArray);
        String name1 = "name 1";
        String type1 = "type 1";
        expect(mockParameterInfo1.getType()).andReturn(type1);
        expect(mockParameterInfo1.getName()).andReturn(name1);
        String value1 = "value 1";
        expect(mockValueConverter.convertParameterValue(name1, type1)).andReturn(value1);

        String name2 = "name 2";
        String type2 = "type 2";
        expect(mockParameterInfo2.getType()).andReturn(type2);
        expect(mockParameterInfo2.getName()).andReturn(name2);
        String value2 = "value 2";
        expect(mockValueConverter.convertParameterValue(name2, type2)).andReturn(value2);

        String operationName = "some_operation_name";
        expect(mockOperationInfo.getName()).andReturn(operationName);

        Object value = new Object();
        expect(mockMBeanServer.invoke(eq(mockObjectName), eq(operationName), anyObject(String[].class), anyObject(String[].class))).andReturn(value);

        replay(mockMBeanServer, mockObjectName, mockOperationInfo, mockValueConverter,
                mockParameterInfo1, mockParameterInfo2);

        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put(name1, new String[] { value1 });
        parameterMap.put(name2, new String[] { value2 });

        Object invokeResult = invoker.invokeOperation(parameterMap);

        assertEquals(value, invokeResult);
        verify(mockMBeanServer, mockObjectName, mockOperationInfo, mockValueConverter,
                mockParameterInfo1, mockParameterInfo2);
    }
}
