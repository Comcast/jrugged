/* DiscreteInterval.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.interval;

/** Representation of integer (long) intervals and associated arithmetic.
 * This is useful for representing numbers with inherent measurement error
 * or uncertainty; instead of a single value or a single value with a +/-
 * error, we represent this as a range of values. One of the interesting
 * properties of interval arithmetic is that errors are monotonically
 * nondecreasing as you perform additional operations on them.
 */
public class DiscreteInterval {

    final private long min;
    final private long max;

    /** Creates an interval spanning a range between the two provided
     * value arguments, inclusive. Order matters for the arguments due
     * to wraparound overflow of longs; the range is defined as all the
     * values crossed by starting at 'min' and then incrementing until
     * 'max' is reached. Thus, this is the closed interval [min, max].
     * @param min minimum value of range
     * @param max maximum value of range
     */
    public DiscreteInterval(long min, long max) {
        this.min = min;
        this.max = max;
    }

    /** Returns the minimum value included in the interval. */
    public long getMin() {
        return min;
    }

    /** Returns the maximum value included in the interval. */
    public long getMax() {
        return max;
    }

    /** Returns the number of discrete values covered by this
     * range.
     */
    public long size() {
        return max - min + 1;
    }

    /** Returns an interval representing the addition of the
     * given interval with this one.
     * @param other interval to add to this one
     * @return interval sum
     */
    public DiscreteInterval plus(DiscreteInterval other) {
        return new DiscreteInterval(this.min + other.min, this.max + other.max);
    }

    /** Returns an interval representing the negation of the
     * closed interval [-1,-1] with this one.
     * @return interval negation
     */
    public DiscreteInterval negate() {
        return new DiscreteInterval(-1 * this.max, -1 * this.min);
    }

    /** Returns an interval representing the subtraction of the
     * given interval from this one.
     * @param other interval to subtract from this one
     * @return result of subtraction
     */
    public DiscreteInterval minus(DiscreteInterval other) {
        return new DiscreteInterval(this.min - other.max, this.max - other.min);
    }

    /** Determines whether the given interval is completely
     * contained by this one.
     */
    public boolean contains(DiscreteInterval other) {
        return (min <= other.getMin() && max >= other.getMax());
    }

    @Override
    public String toString() {
        return "[" + min + "," + max + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (max ^ (max >>> 32));
        result = prime * result + (int) (min ^ (min >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscreteInterval other = (DiscreteInterval) obj;
        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        return true;
    }
}

