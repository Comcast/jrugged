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
import org.fishwife.jrugged.spring.monitor.AbstractMonitor;
import org.fishwife.jrugged.spring.monitor.RawStatisticsMonitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "jrugged:name=exampleMethodCallOne")
public class ExampleSpringPerformanceMonitor extends AbstractMonitor implements InitializingBean, RawStatisticsMonitor {

    protected PerformanceMonitor getMonitor() {
        return monitorInterceptor.getMonitor("exampleMethodCallOne");
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @ManagedAttribute
    public long getFailureCount() {
        return delegatingMonitor.getFailureCount(getMonitor());
    }

    @ManagedAttribute
    public long getRequestCount() {
        return delegatingMonitor.getRequestCount(getMonitor());
    }

    @ManagedAttribute
    public long getSuccessCount() {
        return delegatingMonitor.getSuccessCount(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastHour() {
        return delegatingMonitor.getAverageFailureLatencyLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastHour() {
        return delegatingMonitor.getAverageSuccessLatencyLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastHour() {
        return delegatingMonitor.getRequestRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastDay() {
        return delegatingMonitor.getAverageFailureLatencyLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getAverageFailureLatencyLastMinute() {
        return delegatingMonitor.getAverageFailureLatencyLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastDay() {
        return delegatingMonitor.getAverageSuccessLatencyLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getAverageSuccessLatencyLastMinute() {
        return delegatingMonitor.getAverageSuccessLatencyLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastDay() {
        return delegatingMonitor.getFailureRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastHour() {
        return delegatingMonitor.getFailureRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLastMinute() {
        return delegatingMonitor.getFailureRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getFailureRateLifetime() {
        return delegatingMonitor.getFailureRateLifetime(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastDay() {
        return delegatingMonitor.getRequestRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLastMinute() {
        return delegatingMonitor.getRequestRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getRequestRateLifetime() {
        return delegatingMonitor.getRequestRateLifetime(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastDay() {
        return delegatingMonitor.getSuccessRateLastDay(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastHour() {
        return delegatingMonitor.getSuccessRateLastHour(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLastMinute() {
        return delegatingMonitor.getSuccessRateLastMinute(getMonitor());
    }

    @ManagedAttribute
    public double getSuccessRateLifetime() {
        return delegatingMonitor.getSuccessRateLifetime(getMonitor());
    }

}