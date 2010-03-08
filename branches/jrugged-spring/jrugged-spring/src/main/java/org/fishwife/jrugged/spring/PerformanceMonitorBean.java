/* PerformanceMonitor.java
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
package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.PerformanceMonitor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/** This is basically a {@link PerformanceMonitor} that adds JMX
 * annotations to some of the methods so that the core library
 * doesn't have to depend on spring-context.
 */
@ManagedResource
public class PerformanceMonitorBean extends PerformanceMonitor {
	
	public PerformanceMonitorBean() { super(); }

    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastMinute() {
		return super.getAverageSuccessLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastHour() {
		return super.getAverageSuccessLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastDay() {
		return super.getAverageSuccessLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastMinute() {
		return super.getAverageFailureLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastHour() {
		return super.getAverageFailureLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastDay() {
		return super.getAverageFailureLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public double getRequestRateLastMinute() {
		return super.getRequestRateLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRateLastMinute() {
		return super.getSuccessRateLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getFailureRateLastMinute() {
		return super.getFailureRateLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getRequestRateLastHour() {
		return super.getRequestRateLastHour();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRateLastHour() {
		return super.getSuccessRateLastHour();
    }

    @ManagedAttribute
    @Override
    public double getFailureRateLastHour() {
		return super.getFailureRateLastHour();
    }

    @ManagedAttribute
    @Override
    public double getRequestRateLastDay() {
		return super.getRequestRateLastDay();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRateLastDay() {
		return super.getSuccessRateLastDay();
    }

    @ManagedAttribute
    @Override
    public double getFailureRateLastDay() {
		return super.getFailureRateLastDay();
    }

    @ManagedAttribute
    @Override
    public double getRequestRateLifetime() {
		return super.getRequestRateLifetime();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRateLifetime() {
        return super.getSuccessRateLifetime();
    }

    @ManagedAttribute
    @Override
    public double getFailureRateLifetime() {
		return super.getFailureRateLifetime();
    }

    @ManagedAttribute
    @Override
    public long getRequestCount() {
		return super.getRequestCount();
    }

    @ManagedAttribute
    @Override
    public long getSuccessCount() {
		return super.getSuccessCount();
    }

    @ManagedAttribute
    @Override
    public long getFailureCount() {
		return super.getFailureCount();
    }
}