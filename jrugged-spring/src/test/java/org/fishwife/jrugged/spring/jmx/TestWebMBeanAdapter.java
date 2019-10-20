/* TestWebMBeanAdapter.java
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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

public class TestWebMBeanAdapter {

	private MBeanServer mockMBeanServer;
	private MBeanStringSanitizer mockSanitizer;
	private WebMBeanAdapter webMBeanAdapter;
	private ObjectName mockObjectName;
	private MBeanInfo mockMBeanInfo;
	private MBeanOperationInvoker mockMBeanOperationInvoker;
	private static final String ENCODING = "UTF-8";

	class MockWebMBeanAdapter extends WebMBeanAdapter {
		public MockWebMBeanAdapter(MBeanServer mBeanServer, String mBeanName)
				throws JMException, UnsupportedEncodingException {
			super(mBeanServer, mBeanName, ENCODING);
		}

		@Override
		MBeanStringSanitizer createMBeanStringSanitizer() {
			return mockSanitizer;
		}

		@Override
		ObjectName createObjectName(String name) {
			return mockObjectName;
		}

		@Override
		MBeanOperationInvoker createMBeanOperationInvoker(MBeanServer mBeanServer, ObjectName objectName,
				MBeanOperationInfo operationInfo) {
			return mockMBeanOperationInvoker;
		}
	}

	@Before
	public void setUp() throws Exception {
		mockMBeanServer = createMock(MBeanServer.class);
		mockSanitizer = createMock(MBeanStringSanitizer.class);
		mockMBeanInfo = createMock(MBeanInfo.class);
		mockObjectName = createMock(ObjectName.class);
		mockMBeanOperationInvoker = createMock(MBeanOperationInvoker.class);

		String beanName = "some_bean_name";

		expect(mockSanitizer.urlDecode(beanName, ENCODING)).andReturn(beanName);
		expect(mockMBeanServer.getMBeanInfo(mockObjectName)).andReturn(mockMBeanInfo);
		replay(mockSanitizer, mockMBeanServer);

		webMBeanAdapter = new MockWebMBeanAdapter(mockMBeanServer, beanName);

		verify(mockMBeanServer, mockSanitizer);
		reset(mockMBeanServer, mockSanitizer);
	}

	@Test
	public void testConstructor() throws Exception {
		assertEquals(mockMBeanServer, ReflectionTestUtils.getField(webMBeanAdapter, "mBeanServer"));
		assertEquals(mockObjectName, ReflectionTestUtils.getField(webMBeanAdapter, "objectName"));
		assertEquals(mockMBeanInfo, ReflectionTestUtils.getField(webMBeanAdapter, "mBeanInfo"));
	}

	@Test
	public void testGetAttributeMetadata() throws Exception {

		String attributeName1 = "attribute_name_1";
		MBeanAttributeInfo mockAttribute1 = createMock(MBeanAttributeInfo.class);

		String attributeName2 = "attribute_name_2";
		MBeanAttributeInfo mockAttribute2 = createMock(MBeanAttributeInfo.class);

		MBeanAttributeInfo[] attributeList = new MBeanAttributeInfo[2];
		attributeList[0] = mockAttribute1;
		attributeList[1] = mockAttribute2;
		expect(mockMBeanInfo.getAttributes()).andReturn(attributeList);
		expect(mockAttribute1.getName()).andReturn(attributeName1);
		expect(mockAttribute2.getName()).andReturn(attributeName2);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockAttribute1, mockAttribute2);

		Map<String, MBeanAttributeInfo> attributeMap = webMBeanAdapter.getAttributeMetadata();

		assertEquals(2, attributeMap.size());
		assertEquals(mockAttribute1, attributeMap.get(attributeName1));
		assertEquals(mockAttribute2, attributeMap.get(attributeName2));

		verify(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockAttribute1, mockAttribute2);
	}

	@Test
	public void testGetOperationMetadata() throws Exception {
		String operationName1 = "operation_name_1";
		MBeanOperationInfo mockOperation1 = createMock(MBeanOperationInfo.class);

		String operationName2 = "operation_name_2";
		MBeanOperationInfo mockOperation2 = createMock(MBeanOperationInfo.class);

		MBeanOperationInfo[] operationList = new MBeanOperationInfo[2];
		operationList[0] = mockOperation1;
		operationList[1] = mockOperation2;
		expect(mockMBeanInfo.getOperations()).andReturn(operationList);
		expect(mockOperation1.getName()).andReturn(operationName1);
		expect(mockOperation2.getName()).andReturn(operationName2);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation1, mockOperation2);

		Map<String, MBeanOperationInfo> operationMap = webMBeanAdapter.getOperationMetadata();

		assertEquals(2, operationMap.size());
		assertEquals(mockOperation1, operationMap.get(operationName1));
		assertEquals(mockOperation2, operationMap.get(operationName2));

		verify(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation1, mockOperation2);
	}

	@Test
	public void testGetOperationInfoWhenItExists() throws Exception {
		String operationName1 = "operation_name_1";
		MBeanOperationInfo mockOperation1 = createMock(MBeanOperationInfo.class);

		String operationName2 = "operation_name_2";
		MBeanOperationInfo mockOperation2 = createMock(MBeanOperationInfo.class);

		expect(mockSanitizer.urlDecode(operationName2, ENCODING)).andReturn(operationName2);
		MBeanOperationInfo[] operationList = new MBeanOperationInfo[2];
		operationList[0] = mockOperation1;
		operationList[1] = mockOperation2;
		expect(mockMBeanInfo.getOperations()).andReturn(operationList);
		expect(mockOperation1.getName()).andReturn(operationName1);
		expect(mockOperation2.getName()).andReturn(operationName2);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation1, mockOperation2);

		MBeanOperationInfo operationInfo = webMBeanAdapter.getOperationInfo(operationName2);

		assertEquals(mockOperation2, operationInfo);

		verify(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation1, mockOperation2);
	}

	@Test(expected = OperationNotFoundException.class)
	public void testGetOperationInfoThrowsOperationNotFoundException() throws Exception {
		String operationName = "nonexistent";
		expect(mockObjectName.getCanonicalName()).andReturn("some_name");

		MBeanOperationInfo[] operationList = new MBeanOperationInfo[0];
		expect(mockSanitizer.urlDecode(operationName, ENCODING)).andReturn(operationName);
		expect(mockMBeanInfo.getOperations()).andReturn(operationList);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo);

		webMBeanAdapter.getOperationInfo(operationName);
	}

	@Test
	public void testGetAttributeValues() throws Exception {
		MBeanAttributeInfo[] attributeInfoArray = new MBeanAttributeInfo[2];
		MBeanAttributeInfo mockAttributeInfo1 = createMock(MBeanAttributeInfo.class);
		MBeanAttributeInfo mockAttributeInfo2 = createMock(MBeanAttributeInfo.class);
		attributeInfoArray[0] = mockAttributeInfo1;
		attributeInfoArray[1] = mockAttributeInfo2;
		expect(mockMBeanInfo.getAttributes()).andReturn(attributeInfoArray);

		String attributeName1 = "attribute_name_1";
		String attributeName2 = "attribute_name_2";
		expect(mockAttributeInfo1.getName()).andReturn(attributeName1);
		expect(mockAttributeInfo2.getName()).andReturn(attributeName2);

		AttributeList mockAttributeList = createMock(AttributeList.class);
		expect(mockMBeanServer.getAttributes(eq(mockObjectName), anyObject(String[].class)))
				.andReturn(mockAttributeList);

		List<Attribute> attributeList = new ArrayList<Attribute>();
		Attribute mockAttribute1 = createMock(Attribute.class);
		Attribute mockAttribute2 = createMock(Attribute.class);

		attributeList.add(mockAttribute1);
		attributeList.add(mockAttribute2);

		expect(mockAttributeList.asList()).andReturn(attributeList);

		String name1 = "name 1";
		String value1 = "value 1";
		expect(mockAttribute1.getName()).andReturn(name1);
		expect(mockAttribute1.getValue()).andReturn(value1);
		expect(mockSanitizer.escapeValue(value1)).andReturn(value1);

		String name2 = "name 2";
		String value2 = "value 2";
		expect(mockAttribute2.getName()).andReturn(name2);
		expect(mockAttribute2.getValue()).andReturn(value2);
		expect(mockSanitizer.escapeValue(value2)).andReturn(value2);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockAttributeInfo1, mockAttributeInfo2,
				mockAttributeList, mockAttribute1, mockAttribute2);

		Map<String, Object> attributeValueMap = webMBeanAdapter.getAttributeValues();

		assertEquals(2, attributeValueMap.size());
		assertEquals(value1, attributeValueMap.get(name1));
		assertEquals(value2, attributeValueMap.get(name2));

		verify(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockAttributeInfo1, mockAttributeInfo2,
				mockAttributeList, mockAttribute1, mockAttribute2);
	}

	@Test
	public void testGetAttributeValue() throws Exception {
		String attributeName = "attribute_name";
		expect(mockSanitizer.urlDecode(attributeName, ENCODING)).andReturn(attributeName);

		Object value = new Object();
		String valueString = "some_string";
		expect(mockMBeanServer.getAttribute(mockObjectName, attributeName)).andReturn(value);
		expect(mockSanitizer.escapeValue(value)).andReturn(valueString);

		replay(mockMBeanServer, mockSanitizer, mockObjectName);

		String attributeValue = webMBeanAdapter.getAttributeValue(attributeName);

		assertEquals(valueString, attributeValue);
		verify(mockMBeanServer, mockSanitizer, mockObjectName);
	}

	@Test
	public void testInvokeOperation() throws Exception {
		String operationName = "operation_name";
		expect(mockSanitizer.urlDecode(operationName, ENCODING)).andReturn(operationName);

		MBeanOperationInfo mockOperation = createMock(MBeanOperationInfo.class);
		MBeanOperationInfo[] operationList = new MBeanOperationInfo[1];
		operationList[0] = mockOperation;
		expect(mockMBeanInfo.getOperations()).andReturn(operationList);
		expect(mockOperation.getName()).andReturn(operationName);

		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		Object value = new Object();
		expect(mockMBeanOperationInvoker.invokeOperation(parameterMap)).andReturn(value);

		String valueString = "some_value";
		expect(mockSanitizer.escapeValue(value)).andReturn(valueString);

		replay(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation, mockMBeanOperationInvoker);

		String invokeResult = webMBeanAdapter.invokeOperation(operationName, parameterMap);

		assertEquals(valueString, invokeResult);
		verify(mockMBeanServer, mockSanitizer, mockObjectName, mockMBeanInfo, mockOperation, mockMBeanOperationInvoker);
	}
}
