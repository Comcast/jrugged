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
 * Marks an ExceptionCircuit.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionCircuit {

    /**
     * Name of the circuit.  Each annotation with a shared name shares the same
     * CircuitBreaker.
     */
    String name();

    /**
     * Exception type which trips the CircuitBreaker.
     */
    Class<? extends Throwable>[] trip() default {Exception.class};

    /**
     * Exception type which trips the CircuitBreaker.
     */
    Class<? extends Throwable>[] ignore() default {};

    /**
     * Number of exceptions which must occur in <code>period</code> to trip
     * the circuit
     */
    int frequency() default -1;

    /**
     * Period of time in which <code>frequency</code> errors must occur to
     * trip the circuit.
     */
    long period() default -1;

    /**
     * Amount of time after which the circuit is reset into an open state.
     */
    long reset() default -1;

}