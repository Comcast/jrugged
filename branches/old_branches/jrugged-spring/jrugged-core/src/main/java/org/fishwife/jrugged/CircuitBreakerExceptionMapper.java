/* Copyright 2009-2010 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged;

/**
 * Allows the user to map the standard {@link CircuitBreakerException}
 * thrown by a tripped {@link CircuitBreaker} into an application
 * specific exception.
 *
 * @param <T> is the application specific exception type.
 */
public interface CircuitBreakerExceptionMapper<T extends Exception> {

    /**
     * Turns a {@link CircuitBreakerException} into the desired exception (T)
     *
     * @param breaker the {@link CircuitBreaker}
     * @param e the <code>CircuitBreakerException</code> I get
     * @return the {@link Exception} I want thrown instead
     */
    public T map(CircuitBreaker breaker, CircuitBreakerException e);
    
}
