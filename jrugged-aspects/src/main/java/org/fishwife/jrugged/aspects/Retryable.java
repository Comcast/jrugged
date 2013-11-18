/* Copyright 2009-2013 Comcast Interactive Media, LLC.

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
 * {@link org.fishwife.jrugged.aspects.RetryableAspect}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {

    /**
     * Throwable types that the {@link org.fishwife.jrugged.aspects.RetryableAspect}
     * will retry on.  An empty list indicates that the method call will be
     * retried on ANY Throwable.
     * @return the Throwable types.
     */
    Class<? extends Throwable>[] retryOn() default {};

    /**
     * Specifies the maximum number of tries.
     * @return the maximum number of tries.
     */
    int maxTries() default 2;

    /**
     * Amount of time in milliseconds between retries.
     * @return the amount of time in milliseconds.
     */
    int retryDelayMillis() default 0;

    /**
     * Whether the delay should be doubled between tries.  The delay will reset
     * to the original 'retryDelayMillis' values after a successful call or
     * after 'maxTries' is reached.
     * @return whether the delay should be doubled between tries.
     */
    boolean doubleDelay() default false;

    /**
     * Whether the root cause Exception should be thrown, or whether a generic
     * Exception("Call failed n times") should be thrown.
     * @return if the root cause Exception should be thrown.
     */
    boolean throwCauseException() default true;
}
