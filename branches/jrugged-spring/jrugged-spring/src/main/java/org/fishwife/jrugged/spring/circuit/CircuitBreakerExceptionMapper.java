/* Copyright 2009 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.spring.circuit;

import org.fishwife.jrugged.circuit.CircuitBreakerException;
import org.aopalliance.intercept.MethodInvocation;

public interface CircuitBreakerExceptionMapper<T extends Exception> {

    public T map(MethodInvocation invocation, CircuitBreakerException e);

}