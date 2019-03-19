/* MBeanOperationInvoker.java
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

import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Map;

/**
 * The MBeanOperationInvoker is used to invoke an operation on an MBean.
 */
public class MBeanOperationInvoker {
	MBeanServer mBeanServer;
	ObjectName objectName;
	MBeanOperationInfo operationInfo;

	/**
	 * Constructor.
	 * 
	 * @param mBeanServer   the {@link MBeanServer}.
	 * @param objectName    the {@link ObjectName} for the MBean.
	 * @param operationInfo the {@link MBeanOperationInfo} for the Operation to
	 *                      invoke.
	 */
	public MBeanOperationInvoker(MBeanServer mBeanServer, ObjectName objectName, MBeanOperationInfo operationInfo) {
		this.mBeanServer = mBeanServer;
		this.objectName = objectName;
		this.operationInfo = operationInfo;
	}

	/**
	 * Invoke the operation.
	 * 
	 * @param parameterMap the {@link Map} of parameter names to value arrays.
	 * @return the {@link Object} return value from the operation.
	 * @throws JMException Java Management Exception
	 */
	public Object invokeOperation(Map<String, String[]> parameterMap) throws JMException {
		MBeanParameterInfo[] parameterInfoArray = operationInfo.getSignature();

		Object[] values = new Object[parameterInfoArray.length];
		String[] types = new String[parameterInfoArray.length];

		MBeanValueConverter valueConverter = createMBeanValueConverter(parameterMap);

		for (int parameterNum = 0; parameterNum < parameterInfoArray.length; parameterNum++) {
			MBeanParameterInfo parameterInfo = parameterInfoArray[parameterNum];
			String type = parameterInfo.getType();
			types[parameterNum] = type;
			values[parameterNum] = valueConverter.convertParameterValue(parameterInfo.getName(), type);
		}

		return mBeanServer.invoke(objectName, operationInfo.getName(), values, types);
	}

	MBeanValueConverter createMBeanValueConverter(Map<String, String[]> parameterMap) {
		return new MBeanValueConverter(parameterMap);
	}
}
