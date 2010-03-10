/* PerformanceMonitor.java
 *
 * Copyright 2009-2010 Comcast Interactive Media, LLC.
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
    public PerformanceMonitorBean() {
        super();
    }

	/** Returns the one-minute moving average of the latency of successful
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastMinute() {
		return super.getAverageSuccessLatencyLastMinute();
    }

	/** Returns the one-hour moving average of the latency of successful
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastHour() {
		return super.getAverageSuccessLatencyLastHour();
    }

	/** Returns the 24-hour moving average of the latency of successful
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageSuccessLatencyLastDay() {
		return super.getAverageSuccessLatencyLastDay();
    }

	/** Returns the one-minute moving average of the latency of failed
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastMinute() {
		return super.getAverageFailureLatencyLastMinute();
    }

	/** Returns the one-hour moving average of the latency of failed
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastHour() {
		return super.getAverageFailureLatencyLastHour();
    }

	/** Returns the 24-hour moving average of the latency of failed
	 *  requests, measured in milliseconds.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getAverageFailureLatencyLastDay() {
		return super.getAverageFailureLatencyLastDay();
    }

	/** Returns the one-minute moving average rate of all requests (both
	 *  successes and failures) measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLastMinute() {
		return super.getTotalRequestsPerSecondLastMinute();
    }

	/** Returns the one-minute moving average rate of successful requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastMinute() {
		return super.getSuccessRequestsPerSecondLastMinute();
    }

	/** Returns the one-minute moving average rate of failed requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastMinute() {
		return super.getFailureRequestsPerSecondLastMinute();
    }

	/** Returns the one-hour moving average rate of all requests (both
	 *  successes and failures) measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLastHour() {
		return super.getTotalRequestsPerSecondLastHour();
    }

	/** Returns the one-hour moving average rate of successful requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastHour() {
		return super.getSuccessRequestsPerSecondLastHour();
    }

	/** Returns the one-hour moving average rate of failed requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastHour() {
		return super.getFailureRequestsPerSecondLastHour();
    }

	/** Returns the 24-hour moving average rate of all requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLastDay() {
		return super.getTotalRequestsPerSecondLastDay();
    }

	/** Returns the 24-hour moving average rate of successful requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLastDay() {
		return super.getSuccessRequestsPerSecondLastDay();
    }

	/** Returns the 24-hour moving average rate of failed requests
	 *  measured in requests per second.
	 *  @return double
	 */
    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLastDay() {
		return super.getFailureRequestsPerSecondLastDay();
    }

	/** Returns the average rate of requests measured in requests per
	 *  second since this object was initialized.
	 *  @return double
	 */
	@ManagedAttribute
    @Override
    public double getTotalRequestsPerSecondLifetime() {
		return super.getTotalRequestsPerSecondLifetime();
    }

    /**
     * Returns the average rate of successful requests, measured in
	 * requests per second since this object was initialized.
     * @return double
     */
    @ManagedAttribute
    @Override
    public double getSuccessRequestsPerSecondLifetime() {
        return super.getSuccessRequestsPerSecondLifetime();
    }

    /**
     * Returns the average rate of failed requests, measured in
     *  requests per second since this object was initialized.
     * @return double
     */
    @ManagedAttribute
    @Override
    public double getFailureRequestsPerSecondLifetime() {
		return super.getFailureRequestsPerSecondLifetime();
    }

    /**
     * Returns the total number of requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    @ManagedAttribute
    @Override
    public long getRequestCount() {
		return super.getRequestCount();
    }

    /**
     * Returns the number of successful requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    @ManagedAttribute
    @Override
    public long getSuccessCount() {
		return super.getSuccessCount();
    }

    /**
     * Returns the number of failed requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    @ManagedAttribute
    @Override
    public long getFailureCount() {
		return super.getFailureCount();
    }
}
