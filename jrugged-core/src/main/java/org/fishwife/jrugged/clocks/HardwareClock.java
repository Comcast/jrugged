/* HardwareClock.java
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
package org.fishwife.jrugged.clocks;

import org.fishwife.jrugged.interval.DiscreteInterval;

/** This interface provides access to the &quot;most accurate system
 * timer&quot; available, but also attempts to determine the actual
 * granularity of the timer (which might be greater than 1ns) and
 * present the measurement error inherent in taking clock measurements.
 */
interface HardwareClock {

    /** Gets the estimated hardware clock granularity. This is the
     * number of nanoseconds that elapse between ticks/updates of
     * the underlying hardware clock.
     * @return granularity in nanoseconds
     */
    long getGranularity();

    /** Get an estimate of the current hardware clock reading,
     * represented as a range of times. The +/- error should be half
     * of the measurement increment, which is the clock granularity
     * in our case. Thus, this effectively returns the hardware clock
     * reading +/- (half granularity), with conservative rounding.
     * @return <code>DiscreteInterval</code> representing the range of values the
     *   current hardware clock time might fall within, represented in
     *   nanoseconds.
     */
    DiscreteInterval getNanoTime();

}