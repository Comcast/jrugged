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

    private final Callable<Double> sampleSuccessLatency =
		new Callable<Double>() {
		public Double call() {
			return (double)latencyTracker.getLastSuccessMillis();
		}
    };
    private final Callable<Double> sampleFailureLatency =
		new Callable<Double>() {
		public Double call() {
			return (double)latencyTracker.getLastFailureMillis();
		}
    };
    private final Callable<Double> sampleRequestRate =
		new Callable<Double>() {
		public Double call() {
			return sampleRate()[0];
		}
    };
    private final Callable<Double> sampleSuccessRate =
		new Callable<Double>() {
		public Double call() {
			return sampleRate()[1];
		}
    };
    private final Callable<Double> sampleFailureRate =
		new Callable<Double>() {
		public Double call() {
			return sampleRate()[2];
		}
    };

    private int updateFrequencySecs;
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

	public PerformanceMonitor(MovingAverageFactory maf) {
		averageSuccessLatencyLastMinute = 
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleSuccessLatency);
		averageSuccessLatencyLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleSuccessLatency);
		averageSuccessLatencyLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleSuccessLatency);
		averageFailureLatencyLastMinute = 
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleFailureLatency);
		averageFailureLatencyLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleFailureLatency);
		averageFailureLatencyLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleFailureLatency);
		requestRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleRequestRate);
		successRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleSuccessRate);
		failureRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleFailureRate);
		requestRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleRequestRate);
		successRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleSuccessRate);
		failureRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleFailureRate);
		requestRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleRequestRate);
		successRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleSuccessRate);
		failureRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleFailureRate);
	}

    public PerformanceMonitor(int updateFrequencySecs) {
		this.updateFrequencySecs = updateFrequencySecs;
		MovingAverageFactory maf = 
			new MovingAverageFactory(updateFrequencySecs);

		averageSuccessLatencyLastMinute = 
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleSuccessLatency);
		averageSuccessLatencyLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleSuccessLatency);
		averageSuccessLatencyLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleSuccessLatency);
		averageFailureLatencyLastMinute = 
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleFailureLatency);
		averageFailureLatencyLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleFailureLatency);
		averageFailureLatencyLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleFailureLatency);
		requestRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleRequestRate);
		successRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleSuccessRate);
		failureRateLastMinute =
			maf.makeMovingAverage(ONE_MINUTE_MILLIS, sampleFailureRate);
		requestRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleRequestRate);
		successRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleSuccessRate);
		failureRateLastHour =
			maf.makeMovingAverage(ONE_HOUR_MILLIS, sampleFailureRate);
		requestRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleRequestRate);
		successRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleSuccessRate);
		failureRateLastDay =
			maf.makeMovingAverage(ONE_DAY_MILLIS, sampleFailureRate);
	    
    }

    private double[] sampleRate() {
		long now = System.currentTimeMillis();
		if (now - lastFlowSampleMillis >
			updateFrequencySecs * 500L) {
			lastFlowSample = flowMeter.sample();
			lastFlowSampleMillis = now;
		}
		return lastFlowSample;
    }

    public <T> T invoke(final Callable<T> c) throws Exception {
		return requestCounter.invoke(new Callable<T>() {
				public T call() throws Exception {
					return latencyTracker.invoke(c);
				}
			});
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
		} catch (RuntimeException re) {
			if (WRAP_MSG.equals(re.getMessage())) {
				throw (Exception)re.getCause();
			} else {
				throw re;
			}
		}
    }

    public <T> T invoke(final Runnable r, T result) throws Exception {
		this.invoke(r);
		return result;
    }

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last minute. */
    public double getAverageSuccessLatencyLastMinute() {
		return averageSuccessLatencyLastMinute.getAverage();
    }

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last hour. */
    public double getAverageSuccessLatencyLastHour() {
		return averageSuccessLatencyLastHour.getAverage();
    }

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last day. */
    public double getAverageSuccessLatencyLastDay() {
		return averageSuccessLatencyLastDay.getAverage();
    }

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last minute. */
    public double getAverageFailureLatencyLastMinute() {
		return averageFailureLatencyLastMinute.getAverage();
    }

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last hour. */
    public double getAverageFailureLatencyLastHour() {
		return averageFailureLatencyLastHour.getAverage();
    }

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last day. */
    public double getAverageFailureLatencyLastDay() {
		return averageFailureLatencyLastDay.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last minute. */
    public double getRequestRateLastMinute() {
		return requestRateLastMinute.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last minute. */
    public double getSuccessRateLastMinute() {
		return successRateLastMinute.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last minute. */
    public double getFailureRateLastMinute() {
		return failureRateLastMinute.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last hour. */
    public double getRequestRateLastHour() {
		return requestRateLastHour.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last hour. */
    public double getSuccessRateLastHour() {
		return successRateLastHour.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last hour. */
    public double getFailureRateLastHour() {
		return failureRateLastHour.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last day. */
    public double getRequestRateLastDay() {
		return requestRateLastDay.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last day. */
    public double getSuccessRateLastDay() {
		return successRateLastDay.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last day. */
    public double getFailureRateLastDay() {
		return failureRateLastDay.getAverage();
    }

    /** Returns the average request rate in requests per second of
     *  all requests, as measured since this object was initialized. */
    public double getRequestRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[0])/(double)deltaT;
    }

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured since this object was
     *  initialized. */
    public double getSuccessRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[1])/(double)deltaT;
    }

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured since this object was
     *  initialized. */
    public double getFailureRateLifetime() {
		long deltaT = System.currentTimeMillis() - startupMillis;
		return ((double)requestCounter.sample()[2])/(double)deltaT;
    }

    /** Returns the total number of requests seen by this {@link
     * PerformanceMonitor}. */
    public long getRequestCount() {
		return requestCounter.sample()[0];
    }

    /** Returns the number of successful requests seen by this {@link
     * PerformanceMonitor}. */
    public long getSuccessCount() {
		return requestCounter.sample()[1];
    }

    /** Returns the number of failed requests seen by this {@link
     * PerformanceMonitor}. */
    public long getFailureCount() {
		return requestCounter.sample()[2];
    }

}