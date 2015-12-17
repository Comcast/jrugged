/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Marks a method as one to be wrapped with a
 * {@link org.fishwife.jrugged.SkepticBreaker}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SkepticBreaker {

    /**
     * Name of the skeptic.  Each annotation with a shared value shares
     * the same SkepticBreaker.
     * @return the value
     */
    String name();

    /**
     * Exception types that the {@link
     * org.fishwife.jrugged.SkepticBreaker} will ignore (pass through
     * transparently without tripping).
     * @return the Exception types.
     */
    Class<? extends Throwable>[] ignore() default {};

    /**
     * Specifies the length of the measurement window for failure
     * tolerances in milliseconds.  i.e. if <code>limit</code>
     * failures occur within <code>windowMillis</code> milliseconds,
     * the breaker will trip.
     * @return the length of the measurement window.
     */
     long windowMillis() default -1;

    /**
     * Specifies the number of failures that must occur within a
     * configured time window in order to trip the skeptic breaker.
     * @return the number of failures.
     */
    int limit() default -1;


    /**
     * Input parameters used to determine length of timers during the
     * OPEN (wait) and CLOSED (good) status of the 
     * {@link org.fishwife.jrugged.SkepticBreaker} 
     */    
    long waitBase() default -1; 
    long waitMult() default -1; 
    long goodBase() default -1; 
    long goodMult() default -1; 
    
    /**
     * Specifies the current level of skepticism of the 
     * {@link org.fishwife.jrugged.SkepticBreaker} given past 
     * performance. 
     * @return the Skeptic Level.
     */  
    long skepticLevel() default -1;
    
    /**
     * Specifies the maximum level of skepticism of the 
     * {@link org.fishwife.jrugged.SkepticBreaker} admissible. 
     * @return the maximum skeptic level.
     */ 
    long maxLevel() default -1; 
}
