/* WebMBeanAdapter.java
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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * The WebMBeanAdapter is used to query MBean Attributes and Operations using
 * web-friendly names.
 *
 * It should be noted that by creating a web interface the JMX beans bypasses the JMX security
 * mechanisms that are built into the JVM.  If there is a need to limit access to the JMX
 * beans then the web interface will need to be secured.
 */
public class WebMBeanAdapter {

    private MBeanServer mBeanServer;
    private ObjectName objectName;
    private MBeanStringSanitizer sanitizer = new MBeanStringSanitizer();
    private MBeanInfo mBeanInfo;

    /**
     * Constructor.
     * @param mBeanServer the {@link MBeanServer}.
     * @param mBeanName the Sanitized MBean name.
     * @throws JMException Java Management Exception
     */
    public WebMBeanAdapter(MBeanServer mBeanServer, String mBeanName)throws JMException {
        this.mBeanServer = mBeanServer;
        objectName = createObjectName(mBeanName);
        mBeanInfo = mBeanServer.getMBeanInfo(objectName);
    }

    /**
     * Get the Attribute metadata for an MBean by name.
     * @return the {@link Map} of {@link String} attribute names to {@link MBeanAttributeInfo} values.
     */
    public Map<String, MBeanAttributeInfo> getAttributeMetadata() {

        MBeanAttributeInfo[] attributeList = mBeanInfo.getAttributes();

        Map<String, MBeanAttributeInfo> attributeMap = new TreeMap<String, MBeanAttributeInfo>();
        for (MBeanAttributeInfo attribute: attributeList) {
            attributeMap.put(attribute.getName(), attribute);
        }
        return attributeMap;
    }

    /**
      * Get the Operation metadata for an MBean by name.
      * @return the {@link Map} of {@link String} operation names to {@link MBeanOperationInfo} values.
      */
    public Map<String, MBeanOperationInfo> getOperationMetadata() {

        MBeanOperationInfo[] operations = mBeanInfo.getOperations();

        Map<String, MBeanOperationInfo> operationMap = new TreeMap<String, MBeanOperationInfo>();
        for (MBeanOperationInfo operation: operations) {
            operationMap.put(operation.getName(), operation);
        }
        return operationMap;
    }

    /**
     * Get the Operation metadata for a single operation on an MBean by name.
     * @param operationName the Operation name.
     * @return the {@link MBeanOperationInfo} for the operation.
     * @throws OperationNotFoundException Method was not found
     */
    public MBeanOperationInfo getOperationInfo(String operationName)
        throws OperationNotFoundException {

        Map<String, MBeanOperationInfo> operationMap = getOperationMetadata();
        if (operationMap.containsKey(operationName)) {
            return operationMap.get(operationName);
        }
        throw new OperationNotFoundException("Could not find operation " + operationName + " on MBean " +
                objectName.getCanonicalName());
    }

    /**
     * Get all the attribute values for an MBean by name.  The values are HTML escaped.
     * @return the {@link Map} of attribute names and values.
     * @throws javax.management.AttributeNotFoundException Unable to find the 'attribute'
     * @throws InstanceNotFoundException unable to find the specific bean
     * @throws ReflectionException unable to interrogate the bean
     */
    public Map<String,Object> getAttributeValues()
        throws AttributeNotFoundException, InstanceNotFoundException, ReflectionException {

         HashSet<String> attributeSet = new HashSet<String>();

        for (MBeanAttributeInfo attributeInfo : mBeanInfo.getAttributes()) {
            attributeSet.add(attributeInfo.getName());
        }

        AttributeList attributeList =
                mBeanServer.getAttributes(objectName, attributeSet.toArray(new String[attributeSet.size()]));

        Map<String, Object> attributeValueMap = new TreeMap<String, Object>();
        for (Attribute attribute : attributeList.asList()) {
            attributeValueMap.put(attribute.getName(), sanitizer.escapeValue(attribute.getValue()));
        }

        return attributeValueMap;
    }

    /**
     * Get the value for a single attribute on an MBean by name.
     * @param attributeName the attribute name.
     * @return the value as a String.
     * @throws JMException Java Management Exception
     */
    public String getAttributeValue(String attributeName) throws JMException {
        return sanitizer.escapeValue(mBeanServer.getAttribute(objectName, attributeName));
    }

    /**
     * Invoke an operation on an MBean by name.
     *   Note that only basic data types are supported for parameter values.
     * @param operationName the operation name.
     * @param parameterMap the {@link Map} of parameter names and value arrays.
     * @return the returned value from the operation.
     * @throws JMException Java Management Exception
     */
    public String invokeOperation(String operationName, Map<String, String[]> parameterMap)
        throws JMException {
        MBeanOperationInfo operationInfo = getOperationInfo(operationName);
        MBeanOperationInvoker invoker = createMBeanOperationInvoker(mBeanServer, objectName, operationInfo);
        return sanitizer.escapeValue(invoker.invokeOperation(parameterMap));
    }

    ObjectName createObjectName(String name) throws MalformedObjectNameException {
        return new ObjectName(name);
    }

    MBeanOperationInvoker createMBeanOperationInvoker(
            MBeanServer mBeanServer, ObjectName objectName, MBeanOperationInfo operationInfo) {
        return new MBeanOperationInvoker(mBeanServer,  objectName, operationInfo);
    }
}
