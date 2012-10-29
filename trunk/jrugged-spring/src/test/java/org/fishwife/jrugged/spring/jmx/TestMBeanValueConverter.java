/* TestMBeanValueConverter.java
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
import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class TestMBeanValueConverter {

    private MBeanValueConverter converter;

    @Before
    public void setUp() {

        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("nullValue", new String[] { "null" });
        parameterMap.put("stringValue", new String[] { "some_string" });
        parameterMap.put("booleanValue", new String[] { "true" });
        parameterMap.put("intValue", new String[] { "123" });
        parameterMap.put("longValue", new String[] { "456" });
        parameterMap.put("floatValue", new String[] { "123.45" });
        parameterMap.put("doubleValue", new String[] { "456.78" });
        converter = new MBeanValueConverter(parameterMap);
    }

    @Test
    public void testNullValue() throws Exception {
       assertEquals(null, converter.convertParameterValue("nullValue", ""));
    }

    @Test
    public void testStringValue() throws Exception {
        assertEquals("some_string", converter.convertParameterValue("stringValue", "java.util.String"));
    }

    @Test
    public void testBooleanValue() throws Exception {
        assertEquals(true, converter.convertParameterValue("booleanValue", "boolean"));
    }

    @Test
    public void testIntValue() throws Exception {
        assertEquals(123, converter.convertParameterValue("intValue", "int"));
    }

    @Test
    public void testLongValue() throws Exception {
        assertEquals((long)456, converter.convertParameterValue("longValue", "long"));
    }

    @Test
    public void testFloatValue() throws Exception {
        assertEquals((float)123.45, converter.convertParameterValue("floatValue", "float"));
    }

    @Test
    public void testDoubleValue() throws Exception {
        assertEquals(456.78, converter.convertParameterValue("doubleValue", "double"));
    }

    @Test(expected=UnhandledParameterTypeException.class)
    public void testUnhandledType() throws Exception {
        converter.convertParameterValue("stringValue", "unknown_type");
    }
}
