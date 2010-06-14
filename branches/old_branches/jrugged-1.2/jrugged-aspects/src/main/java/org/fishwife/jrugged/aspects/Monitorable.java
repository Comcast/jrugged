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

import org.fishwife.jrugged.PerformanceMonitor;

/**
 * Annotation that is used to indicate that a method should be
 * wrapped by a {@link PerformanceMonitor}.  The value passed to the
 * annotation serves as a key for a PerformanceMonitor instance.  You may
 * have PerformanceMonitors for individual methods by using unique key names
 * in the Monitorable annotation for the method, or you may share a
 * PerformanceMonitor across classes and methods by using the same key name
 * for many Monitorable annotations.
 * 
 * @author bschmaus
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Monitorable {
    String value();
}
