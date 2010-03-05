/* DelegatingPerformanceMonitor.java
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
package org.fishwife.jrugged.spring.monitor;

import org.fishwife.jrugged.PerformanceMonitor;

/**
 * Class that delegates to an underlying {@link PerformanceMonitor} or returns
 * a default value if the given {@link PerformanceMonitor} is null.
 */
public class DelegatingPerformanceMonitor {

    private static final long DEFAULT = -1;

    public long getFailureCount(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getFailureCount()
                : DEFAULT;
    }

    public long getRequestCount(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getRequestCount()
                : DEFAULT;
    }

    public long getSuccessCount(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getSuccessCount()
                : DEFAULT;
    }

    public double getAverageFailureLatencyLastHour(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getAverageFailureLatencyLastHour()
                : DEFAULT;
    }

    public double getAverageSuccessLatencyLastHour(PerformanceMonitor monitor) {
        return monitor != null ? monitor
                .getAverageSuccessLatencyLastHour()
                : DEFAULT;
    }

    public double getRequestRateLastHour(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getRequestRateLastHour()
                : DEFAULT;
    }

    public double getAverageFailureLatencyLastDay(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getAverageFailureLatencyLastDay()
                : DEFAULT;
    }

    public double getAverageFailureLatencyLastMinute(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getAverageFailureLatencyLastMinute()
                : DEFAULT;
    }

    public double getAverageSuccessLatencyLastDay(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getAverageSuccessLatencyLastDay()
                : DEFAULT;
    }

    public double getAverageSuccessLatencyLastMinute(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getAverageSuccessLatencyLastMinute()
                : DEFAULT;
    }

    public double getFailureRateLastDay(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getFailureRateLastDay()
                : DEFAULT;
    }

    public double getFailureRateLastHour(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getFailureRateLastHour()
                : DEFAULT;
    }

    public double getFailureRateLastMinute(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getFailureRateLastMinute()
                : DEFAULT;
    }

    public double getFailureRateLifetime(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getFailureRateLifetime()
                : DEFAULT;
    }

    public double getRequestRateLastDay(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getRequestRateLastDay()
                : DEFAULT;
    }

    public double getRequestRateLastMinute(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getRequestRateLastMinute()
                : DEFAULT;
    }

    public double getRequestRateLifetime(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getRequestRateLifetime()
                : DEFAULT;
    }

    public double getSuccessRateLastDay(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getSuccessRateLastDay()
                : DEFAULT;
    }

    public double getSuccessRateLastHour(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getSuccessRateLastHour()
                : DEFAULT;
    }

    public double getSuccessRateLastMinute(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getSuccessRateLastMinute()
                : DEFAULT;
    }

    public double getSuccessRateLifetime(PerformanceMonitor monitor) {
        return monitor != null ? monitor.getSuccessRateLifetime()
                : DEFAULT;
    }
}
