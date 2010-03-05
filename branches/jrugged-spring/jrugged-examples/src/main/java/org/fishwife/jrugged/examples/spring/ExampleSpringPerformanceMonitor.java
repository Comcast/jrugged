/* ExampleSpringPerformanceMonitor.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.examples.spring;

import org.fishwife.jrugged.PerformanceMonitor;
import org.fishwife.jrugged.spring.monitor.RawStatisticsMonitor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "jrugged:name=exampleMethodCallOne")
public class ExampleSpringPerformanceMonitor implements RawStatisticsMonitor {

    protected PerformanceMonitor getMonitor() {
		throw new IllegalStateException("I am broken");
//         return monitorInterceptor.getMonitor("exampleMethodCallOne");
    }

    public String getName() {
		throw new IllegalStateException("I am broken");
//         return this.getClass().getSimpleName();
    }

    @ManagedAttribute
    public long getFailureCount() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getFailureCount(getMonitor());
    }

    @ManagedAttribute
    public long getRequestCount() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getRequestCount(getMonitor());
    }

    @ManagedAttribute
    public long getSuccessCount() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getSuccessCount(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastHour() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageFailureLatencyLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastHour() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageSuccessLatencyLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastHour() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getRequestRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastDay() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageFailureLatencyLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastMinute() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageFailureLatencyLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastDay() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageSuccessLatencyLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastMinute() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getAverageSuccessLatencyLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastDay() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getFailureRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastHour() {
		throw new IllegalStateException("I am broken"); 
//        return delegatingMonitor.getFailureRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastMinute() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getFailureRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLifetime() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getFailureRateLifetime(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastDay() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getRequestRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastMinute() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getRequestRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLifetime() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getRequestRateLifetime(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastDay() {
		throw new IllegalStateException("I am broken");

//         return delegatingMonitor.getSuccessRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastHour() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getSuccessRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastMinute() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getSuccessRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLifetime() {
		throw new IllegalStateException("I am broken");
//         return delegatingMonitor.getSuccessRateLifetime(getMonitor());
    }

}