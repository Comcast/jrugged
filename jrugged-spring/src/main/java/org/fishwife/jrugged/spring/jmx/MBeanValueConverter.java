/* MBeanValueConverter.java
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

import java.util.Map;

/**
 * The MBeanValueConverter is used to convert {@link String} parameter values stored in a {@link Map} of
 * parameter names to values into their native types.
 */
public class MBeanValueConverter {

    private Map<String, String[]> parameterMap;

    /**
     * Constructor.
     * @param parameterMap the {@link Map} of parameter names to {@link String} values.
     */
    public MBeanValueConverter(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Convert the {@link String} parameter value into it's native type.
     *   The {@link String} 'null' is converted into a null value.
     *   Only types String, Boolean, Int, Long, Float, and Double are supported.
     * @param parameterName the parameter name to convert.
     * @param type the native type for the parameter.
     * @return the converted value.
     * @throws NumberFormatException the parameter is not a number
     * @throws UnhandledParameterTypeException unable to recognize the parameter type
     */
    public Object convertParameterValue(String parameterName, String type)
            throws NumberFormatException, UnhandledParameterTypeException {
        String[] valueList = parameterMap.get(parameterName);
        if (valueList == null || valueList.length == 0) return null;
        String value = valueList[0];
        if (value.equals("null")) return null;
        if (type.equals("java.util.String")) return value;
        if (type.equals("boolean")) return Boolean.parseBoolean(value);
        if (type.equals("int")) return Integer.parseInt(value);
        if (type.equals("long")) return Long.parseLong(value);
        if (type.equals("float")) return Float.parseFloat(value);
        if (type.equals("double")) return Double.parseDouble(value);

        throw new UnhandledParameterTypeException("Cannot convert " + value + " into type " + type +
                " for parameter " + parameterName);
    }
}
