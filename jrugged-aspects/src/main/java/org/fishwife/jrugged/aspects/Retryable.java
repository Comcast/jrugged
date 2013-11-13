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
     * Throwable types that the {@link org.fishwife.jrugged.aspects.Retryable}
     * will retry on.  An empty list indicates that the method call will be
     * retried on ANY Throwable.
     * @return the Throwable types.
     */
    Class<? extends Throwable>[] retryOn() default {};

    /**
     * Specifies the maximum number of retries.
     * @return the maximum number of retries.
     */
    int maxRetries() default 0;

    /**
     * Amount of time in milliseconds between retries.
     * @return the amount of time in milliseconds.
     */
    long retryDelayMillis() default 0;
}
