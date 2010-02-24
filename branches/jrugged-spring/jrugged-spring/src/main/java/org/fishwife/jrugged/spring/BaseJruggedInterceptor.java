/* CircuitBreaker.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;

/**
 * Interceptors need to have a way to get the method name easily to
 * understand if the method is one that is involved in monitoring or
 * circuit.  This method below does that for any interceptor.
 */
public abstract class BaseJruggedInterceptor implements MethodInterceptor, InitializingBean {

    public abstract Object invoke(MethodInvocation methodInvocation) throws Throwable;

    protected String getInvocationTraceName(MethodInvocation invocation) {
        StringBuilder sb = new StringBuilder("");
        Method method = invocation.getMethod();
        sb.append(method.getDeclaringClass().getSimpleName());
        sb.append('.').append(method.getName());
        sb.append("");
        return sb.toString();
    }
}
