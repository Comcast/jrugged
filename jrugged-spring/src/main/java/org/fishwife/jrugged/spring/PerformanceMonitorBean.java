/* PerformanceMonitor.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

/** The {@link PerformanceMonitorBean} is a straightforward wrapper
 *  around a {@link PerformanceMonitor} that allows for leveraging
 *  automated exposure of the information via Spring's JMX annotations.
 */
@ManagedResource
public class PerformanceMonitorBean extends PerformanceMonitor {

    /** Constructs a <code>PerformanceMonitorBean</code>. */
    public PerformanceMonitorBean() {
        super();
    }

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
    public double getTotalRequestsPerSecondLastMinute() {
        return super.getTotalRequestsPerSecondLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastMinute() {
        return super.getSuccessRequestsPerSecondLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastMinute() {
        return super.getFailureRequestsPerSecondLastMinute();
    }

    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLastHour() {
        return super.getTotalRequestsPerSecondLastHour();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastHour() {
        return super.getSuccessRequestsPerSecondLastHour();
    }

    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastHour() {
        return super.getFailureRequestsPerSecondLastHour();
    }

    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLastDay() {
        return super.getTotalRequestsPerSecondLastDay();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastDay() {
        return super.getSuccessRequestsPerSecondLastDay();
    }

    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastDay() {
        return super.getFailureRequestsPerSecondLastDay();
    }

    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLifetime() {
        return super.getTotalRequestsPerSecondLifetime();
    }

    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLifetime() {
        return super.getSuccessRequestsPerSecondLifetime();
    }

    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLifetime() {
        return super.getFailureRequestsPerSecondLifetime();
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

    @ManagedAttribute
    @Override
    public long getMedianPercentileSuccessLatencyLifetime() {
        return super.getMedianPercentileSuccessLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileSuccessLatencyLifetime() {
        return super.get95thPercentileSuccessLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileSuccessLatencyLifetime() {
        return super.get99thPercentileSuccessLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long getMaxSuccessLatencyLifetime() {
        return super.getMaxSuccessLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileSuccessLatencyLastMinute() {
        return super.getMedianPercentileSuccessLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileSuccessLatencyLastMinute() {
        return super.get95thPercentileSuccessLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileSuccessLatencyLastMinute() {
        return super.get99thPercentileSuccessLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileSuccessfulLatencyLastHour() {
        return super.getMedianPercentileSuccessfulLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileSuccessLatencyLastHour() {
        return super.get95thPercentileSuccessLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileSuccessLatencyLastHour() {
        return super.get99thPercentileSuccessLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileSuccessLatencyLastDay() {
        return super.getMedianPercentileSuccessLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileSuccessLatencyLastDay() {
        return super.get95thPercentileSuccessLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileSuccessLatencyLastDay() {
        return super.get99thPercentileSuccessLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileFailureLatencyLifetime() {
        return super.getMedianPercentileFailureLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileFailureLatencyLifetime() {
        return super.get95thPercentileFailureLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileFailureLatencyLifetime() {
        return super.get99thPercentileFailureLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long getMaxFailureLatencyLifetime() {
        return super.getMaxFailureLatencyLifetime();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileFailureLatencyLastMinute() {
        return super.getMedianPercentileFailureLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileFailureLatencyLastMinute() {
        return super.get95thPercentileFailureLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileFailureLatencyLastMinute() {
        return super.get99thPercentileFailureLatencyLastMinute();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileFailureLatencyLastHour() {
        return super.getMedianPercentileFailureLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileFailureLatencyLastHour() {
        return super.get95thPercentileFailureLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileFailureLatencyLastHour() {
        return super.get99thPercentileFailureLatencyLastHour();
    }

    @ManagedAttribute
    @Override
    public long getMedianPercentileFailureLatencyLastDay() {
        return super.getMedianPercentileFailureLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public long get95thPercentileFailureLatencyLastDay() {
        return super.get95thPercentileFailureLatencyLastDay();
    }

    @ManagedAttribute
    @Override
    public long get99thPercentileFailureLatencyLastDay() {
        return super.get99thPercentileFailureLatencyLastDay();
    }
}
