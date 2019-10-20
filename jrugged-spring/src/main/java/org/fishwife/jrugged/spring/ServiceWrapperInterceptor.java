/* ServiceWrapperInterceptor.java
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
package org.fishwife.jrugged.spring;

import java.util.Map;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.PerformanceMonitor;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * A Spring interceptor that allows wrapping a method invocation with a
 * {@link ServiceWrapper} (for example, a {@link CircuitBreaker} or
 * {@link PerformanceMonitor}).
 */
public class ServiceWrapperInterceptor implements MethodInterceptor {

	private Map<String, ServiceWrapper> methodMap;

	/**
	 * See if the given method invocation is one that needs to be called through a
	 * {@link ServiceWrapper}, and if so, do so.
	 * 
	 * @param invocation the {@link MethodInvocation} in question
	 * @return whatever the underlying method call would normally return
	 * @throws Throwable that the method call would generate, or that the
	 *                   {@link ServiceWrapper} would generate when tripped.
	 */
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		String methodName = invocation.getMethod().getName();

		if (!shouldWrapMethodCall(methodName)) {
			return invocation.proceed();
		} else {
			ServiceWrapper wrapper = methodMap.get(methodName);

			return wrapper.invoke(new Callable<Object>() {
				public Object call() throws Exception {
					try {
						return invocation.proceed();
					} catch (Throwable e) {
						if (e instanceof Exception)
							throw (Exception) e;
						else if (e instanceof Error)
							throw (Error) e;
						else
							throw new RuntimeException(e);
					}
				}
			});
		}
	}

	private boolean shouldWrapMethodCall(String methodName) {
		if (methodMap == null) {
			return true; // Wrap all by default
		}

		if (methodMap.get(methodName) != null) {
			return true; // Wrap a specific method
		}

		// If I get to this point, I should not wrap the call.
		return false;
	}

	/**
	 * Specifies which methods will be wrapped with which ServiceWrappers.
	 * 
	 * @param methodMap the mapping!
	 */
	public void setMethods(Map<String, ServiceWrapper> methodMap) {
		this.methodMap = methodMap;
	}
}
