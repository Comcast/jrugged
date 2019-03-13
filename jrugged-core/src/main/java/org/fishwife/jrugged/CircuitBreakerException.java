/* CircuitBreakerException.java
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
package org.fishwife.jrugged;

/**
 * This exception gets thrown by a {@link CircuitBreaker} if a wrapped
 * call is disallowed by a tripped (OPEN) breaker.
 */
public class CircuitBreakerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Default constructor. */
    public CircuitBreakerException() { }
}
