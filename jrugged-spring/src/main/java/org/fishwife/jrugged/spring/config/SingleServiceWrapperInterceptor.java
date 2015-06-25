/* SingleServiceWrapperInterceptor.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.spring.config;

import java.util.List;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * Rework of the ServiceWrapperInterceptor that uses a list of methods instead
 * of a map and only supports one ServiceWrapper.
 */
public class SingleServiceWrapperInterceptor implements MethodInterceptor {

    private List<String> methodList;
    private ServiceWrapper serviceWrapper;

    /** See if the given method invocation is one that needs to be
     * called through a {@link ServiceWrapper}, and if so, do so.
     * @param invocation the {@link MethodInvocation} in question
     * @return whatever the underlying method call would normally
     * return
     * @throws Throwable that the method call would generate, or
     *   that the {@link ServiceWrapper} would generate when tripped.
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();

        if (!shouldWrapMethodCall(methodName)) {
            return invocation.proceed();
        }
        else {
            ServiceWrapper wrapper = serviceWrapper;

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

    /**
     * Checks if the method being invoked should be wrapped by a service.
     * It looks the method name up in the methodList. If its in the list, then
     * the method should be wrapped. If the list is null, then all methods
     * are wrapped.
     *
     * @param methodName The method being called
     *
     * @return boolean
     */
    private boolean shouldWrapMethodCall(String methodName) {
        if (methodList == null) {
            return true; // Wrap all by default
        }

        if (methodList.contains(methodName)) {
            return true; //Wrap a specific method
        }

        // If I get to this point, I should not wrap the call.
        return false;
    }

    /**
     * Specifies which methods will be wrapped with the ServiceWrapper.
     *
     * @param methodList the methods I intend to wrap calls around
     */
    public void setMethods(List<String> methodList)  {
        this.methodList = methodList;
    }

    /**
     * Return the ServiceWrapper being used to wrap the methods.
     *
     * @return ServiceWrapper
     */
    public ServiceWrapper getServiceWrapper() {
        return serviceWrapper;
    }

    /**
     * Set the ServiceWrapper to wrap the methods with.
     *
     * @param serviceWrapper The wrapper instance from Spring config.
     */
    public void setServiceWrapper(ServiceWrapper serviceWrapper) {
        this.serviceWrapper = serviceWrapper;
    }
}
