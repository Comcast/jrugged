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
package org.fishwife.jrugged;

import java.util.concurrent.Callable;

/** The {@link PerformanceMonitor} is a convenience wrapper for
 *  gathering a slew of useful operational metrics about a service,
 *  including moving averages for latency and request rate over
 *  various time windows (last minute, last hour, last day).
 *
 *  The intended use is for a client to use the "Decorator" design
 *  pattern that decorates an existing service with this wrapper.
 *  Portions of this object can then be exposed via JMX, for example
 *  to allow for operational polling.
 */
public class PerformanceMonitor implements ServiceWrapper {

    private static final String WRAP_MSG = 
		"org.fishwife.jrugged.PerformanceMonitor.WRAPPED";

    private final long startupMillis = System.currentTimeMillis();

    private static final long ONE_MINUTE_MILLIS = 60L * 1000L;
    private static final long ONE_HOUR_MILLIS = ONE_MINUTE_MILLIS * 60L;
    private static final long ONE_DAY_MILLIS = ONE_HOUR_MILLIS * 24L;

    private final RequestCounter requestCounter = new RequestCounter();
    private final FlowMeter flowMeter = new FlowMeter(requestCounter);
    private final LatencyTracker latencyTracker = new LatencyTracker();

    private long lastFlowSampleMillis;
    private double[] lastFlowSample = { 0.0, 0.0, 0.0 };

    private MovingAverage averageSuccessLatencyLastMinute;
    private MovingAverage averageSuccessLatencyLastHour;
    private MovingAverage averageSuccessLatencyLastDay;
    private MovingAverage averageFailureLatencyLastMinute;
    private MovingAverage averageFailureLatencyLastHour;
    private MovingAverage averageFailureLatencyLastDay;
    private MovingAverage requestRateLastMinute;
    private MovingAverage successRateLastMinute;
    private MovingAverage failureRateLastMinute;
    private MovingAverage requestRateLastHour;
    private MovingAverage successRateLastHour;
    private MovingAverage failureRateLastHour;
    private MovingAverage requestRateLastDay;
    private MovingAverage successRateLastDay;
    private MovingAverage failureRateLastDay;

    public PerformanceMonitor() {
		createMovingAverages();
    }

	private void createMovingAverages() {
		averageSuccessLatencyLastMinute = new MovingAverage(ONE_MINUTE_MILLIS);
		averageSuccessLatencyLastHour = new MovingAverage(ONE_HOUR_MILLIS);
		averageSuccessLatencyLastDay = new MovingAverage(ONE_DAY_MILLIS);
		averageFailureLatencyLastMinute = new MovingAverage(ONE_MINUTE_MILLIS);
		averageFailureLatencyLastHour = new MovingAverage(ONE_HOUR_MILLIS);
		averageFailureLatencyLastDay = new MovingAverage(ONE_DAY_MILLIS);
		requestRateLastMinute = new MovingAverage(ONE_MINUTE_MILLIS);
		successRateLastMinute = new MovingAverage(ONE_MINUTE_MILLIS);
		failureRateLastMinute = new MovingAverage(ONE_MINUTE_MILLIS);
		requestRateLastHour = new MovingAverage(ONE_HOUR_MILLIS);
		successRateLastHour = new MovingAverage(ONE_HOUR_MILLIS);
		failureRateLastHour = new MovingAverage(ONE_HOUR_MILLIS);
		requestRateLastDay = new MovingAverage(ONE_DAY_MILLIS);
		successRateLastDay = new MovingAverage(ONE_DAY_MILLIS);
		failureRateLastDay = new MovingAverage(ONE_DAY_MILLIS);
	}

	private void recordRequest() {
		double[] rates = flowMeter.sample();
		requestRateLastMinute.update(rates[0]);
		requestRateLastHour.update(rates[0]);
		requestRateLastDay.update(rates[0]);

		successRateLastMinute.update(rates[1]);
		successRateLastHour.update(rates[1]);
		successRateLastDay.update(rates[1]);

		failureRateLastHour.update(rates[2]);
		failureRateLastMinute.update(rates[2]);
		failureRateLastDay.update(rates[2]);
	}

	private void recordSuccess() {
		long successMillis = latencyTracker.getLastSuccessMillis();
		averageSuccessLatencyLastMinute.update(successMillis);
		averageSuccessLatencyLastHour.update(successMillis);
		averageSuccessLatencyLastDay.update(successMillis);
		recordRequest();
	}

	private void recordFailure() {
		long failureMillis = latencyTracker.getLastFailureMillis();
		averageFailureLatencyLastMinute.update(failureMillis);
		averageFailureLatencyLastHour.update(failureMillis);
		averageFailureLatencyLastDay.update(failureMillis);
		recordRequest();
	}

    public <T> T invoke(final Callable<T> c) throws Exception {
		try {
			T result = requestCounter.invoke(new Callable<T>() {
					public T call() throws Exception {
						return latencyTracker.invoke(c);
					}
				});
			recordSuccess();
			return result;
		} catch (RuntimeException re) {
			recordFailure();
			if (WRAP_MSG.equals(re.getMessage())) {
				throw (Exception)re.getCause();
			} else {
				throw re;
			}
		}
    }

    public void invoke(final Runnable r) throws Exception {
		try {
			requestCounter.invoke(new Runnable() {
					public void run() {
						try {
							latencyTracker.invoke(r);
						} catch (Exception e) {
							throw new RuntimeException(WRAP_MSG, e);
						}
					}
				});
			recordSuccess();
		} catch (RuntimeException re) {
			recordFailure();
			if (WRAP_MSG.equals(re.getMessage())) {
				throw (Exception)re.getCause();
			} else {
				throw re;
			}
		}
    }

    public <T> T invoke(final Runnable r, T result) throws Exception {
		try {
			this.invoke(r);
			recordSuccess();
			return result;
		} catch (RuntimeException re) {
			recordFailure();
			if (WRAP_MSG.equals(re.getMessage())) {
				throw (Exception)re.getCause();
			} else {
				throw re;
			}
		}
    }

    /**
     * Returns the average latency in milliseconds of a successful request,
     *  as measured over the last minute.
     *
     * @return double
     */
    public double getAverageSuccessLatencyLastMinute() {
		return averageSuccessLatencyLastMinute.getAverage();
    }

    /**
     * Returns the average latency in milliseconds of a successful request,
     *  as measured over the last hour.
     *
     * @return double
     */
    public double getAverageSuccessLatencyLastHour() {
		return averageSuccessLatencyLastHour.getAverage();
    }

    /**
     * Returns the average latency in milliseconds of a successful request,
     *  as measured over the last day.
     *
     * @return double
     */
    public double getAverageSuccessLatencyLastDay() {
		return averageSuccessLatencyLastDay.getAverage();
    }

    /**
     * Returns the average latency in milliseconds of a failed request,
     *  as measured over the last minute.
     *
     * @return double
     */
    public double getAverageFailureLatencyLastMinute() {
		return averageFailureLatencyLastMinute.getAverage();
    }

    /**
     * Returns the average latency in milliseconds of a failed request,
     *  as measured over the last hour.
     *
     * @return double
     */
    public double getAverageFailureLatencyLastHour() {
		return averageFailureLatencyLastHour.getAverage();
    }

    /**
     * Returns the average latency in milliseconds of a failed request,
     *  as measured over the last day.
     *
     * @return double
     */
    public double getAverageFailureLatencyLastDay() {
		return averageFailureLatencyLastDay.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  all requests, as measured over the last minute.
     *
     * @return double
     */
    public double getRequestRateLastMinute() {
		return requestRateLastMinute.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  successful requests, as measured over the last minute.
     *
     * @return double
     */
    public double getSuccessRateLastMinute() {
		return successRateLastMinute.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  failed requests, as measured over the last minute.
     *
     * @return double
     */
    public double getFailureRateLastMinute() {
		return failureRateLastMinute.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  all requests, as measured over the last hour.
     *
     * @return double
     */
    public double getRequestRateLastHour() {
		return requestRateLastHour.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  successful requests, as measured over the last hour.
     *
     * @return double
     */
    public double getSuccessRateLastHour() {
		return successRateLastHour.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last hour.
     *
     * @return double
     */
    public double getFailureRateLastHour() {
		return failureRateLastHour.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  all requests, as measured over the last day.
     *
     * @return double
     */
    public double getRequestRateLastDay() {
		return requestRateLastDay.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  successful requests, as measured over the last day.
     *
     * @return double
     */
    public double getSuccessRateLastDay() {
		return successRateLastDay.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  failed requests, as measured over the last day.
     *
     * @return double
     */
    public double getFailureRateLastDay() {
		return failureRateLastDay.getAverage();
    }

    /**
     * Returns the average request rate in requests per second of
     *  all requests, as measured since this object was initialized.
     *
     * @return double
     */
    public double getRequestRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[0])/(double)deltaT;
    }

    /**
     * Returns the average request rate in requests per second of
     *  successful requests, as measured since this object was
     *  initialized.
     *
     * @return double
     */
    public double getSuccessRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[1])/(double)deltaT;
    }

    /**
     * Returns the average request rate in requests per second of
     *  failed requests, as measured since this object was
     *  initialized.
     *
     * @return double
     */
    public double getFailureRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[2])/(double)deltaT;
    }

    /**
     * Returns the total number of requests seen by this {@link
     * PerformanceMonitor}.
     *
     * @return long
     */
    public long getRequestCount() {
		return requestCounter.sample()[0];
    }

    /**
     * Returns the number of successful requests seen by this {@link
     * PerformanceMonitor}.
     *
     * @return long
     */
    public long getSuccessCount() {
		return requestCounter.sample()[1];
    }

    /**
     * Returns the number of failed requests seen by this {@link
     * PerformanceMonitor}.
     *
     * @return long
     */
    public long getFailureCount() {
		return requestCounter.sample()[2];
    }
}