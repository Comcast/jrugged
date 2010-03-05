/* RawStatisticsMonitor.java
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
 * Defines common monitorable attributes.
 */
public interface RawStatisticsMonitor {

    public String getName();

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last minute.
     * @return double
     */
    public double getAverageSuccessLatencyLastMinute();

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last hour.
     * @return double
     */
    public double getAverageSuccessLatencyLastHour();

    /** Returns the average latency in milliseconds of a successful request,
     *  as measured over the last day.
     * @return double
     */
    public double getAverageSuccessLatencyLastDay();

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last minute.
     * @return double
     */
    public double getAverageFailureLatencyLastMinute();

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last hour.
     * @return double
     */
    public double getAverageFailureLatencyLastHour();

    /** Returns the average latency in milliseconds of a failed request,
     *  as measured over the last day.
     * @return double
     */
    public double getAverageFailureLatencyLastDay();

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last minute.
     * @return double
     */
    public double getRequestRateLastMinute();

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last minute.
     * @return double
     */
    public double getSuccessRateLastMinute();

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last minute.
     * @return double
     */
    public double getFailureRateLastMinute();

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last hour.
     * @return double
     */
    public double getRequestRateLastHour();

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last hour.
     * @return double
     */
    public double getSuccessRateLastHour();

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last hour.
     * @return double
     */
    public double getFailureRateLastHour();

    /** Returns the average request rate in requests per second of
     *  all requests, as measured over the last day.
     * @return double
     */
    public double getRequestRateLastDay();

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured over the last day.
     * @return double
     */
    public double getSuccessRateLastDay();

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured over the last day.
     * @return double
     */
    public double getFailureRateLastDay();

    /** Returns the average request rate in requests per second of
     *  all requests, as measured since this object was initialized.
     * @return double
     */
    public double getRequestRateLifetime();

    /** Returns the average request rate in requests per second of
     *  successful requests, as measured since this object was
     *  initialized.
     * @return double
     */
    public double getSuccessRateLifetime();

    /** Returns the average request rate in requests per second of
     *  failed requests, as measured since this object was
     *  initialized.
     * @return double
     */
    public double getFailureRateLifetime();

    /** Returns the total number of requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    public long getRequestCount();

    /** Returns the number of successful requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    public long getSuccessCount();

    /** Returns the number of failed requests seen by this {@link
     * PerformanceMonitor}.
     * @return long
     */
    public long getFailureCount();

}
