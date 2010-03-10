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
package org.fishwife.jrugged.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one to be wrapped with a
 * {@link org.fishwife.jrugged.CircuitBreaker}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CircuitBreaker {

    /**
     * Name of the circuit.  Each annotation with a shared name shares
     * the same CircuitBreaker.
     */
    String name();

    /**
     * Exception types that the {@link
     * org.fishwife.jrugged.CircuitBreaker} will ignore (pass through
     * transparently without tripping).
     */
    Class<? extends Throwable>[] ignore() default {};

    /**
     * Specifies the length of the measurement window for failure
     * tolerances in milliseconds.  i.e. if <code>limit</code>
     * failures occur within <code>windowMillis</code> milliseconds,
     * the breaker will trip.
     */
     long windowMillis() default -1;

    /**
     * Specifies the number of failures that must occur within a
     * configured time window in order to trip the circuit breaker.
     */
    int limit() default -1;


    /**
     * Amount of time in milliseconds after tripping after which the
     * {@link org.fishwife.jrugged.CircuitBreaker} is reset and will
	 * allow a test request through.
     */
    long resetMillis() default -1;

}