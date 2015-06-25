/* TestClass.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.spring.testonly;

import org.fishwife.jrugged.aspects.CircuitBreaker;
import org.fishwife.jrugged.aspects.PerformanceMonitor;
import org.junit.Ignore;

// Test class under test package to test package scan
@Ignore
public class TestClass {

    @PerformanceMonitor("monitorA")
    public void monitoredMethod1() {

    }

    @PerformanceMonitor("monitorB")
    public void monitoredMethod2() {

    }

    @PerformanceMonitor("monitorA")
    public void monitoredMethod3() {

    }

    @CircuitBreaker(name = "breakerA")
    public void circuitBreakerMethod1() {

    }

    @CircuitBreaker(name = "breakerB")
    public void circuitBreakerMethod2() {

    }

    public void unmonitoredMethod() {

    }

}
