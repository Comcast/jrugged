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
     * value arguments, inclusive. No requirements placed on relative
     * ordering of the two arguments. This represents the closed 
     * interval [min(v1,v2), max(v1,v2)].
     * @param v1 first value
     * @param v2 second value
     */
    public DiscreteInterval(long v1, long v2) {
        if (v1 > v2) {
            this.min = v2;
            this.max = v1;
        } else {
            this.min = v1;
            this.max = v2;
        }
    }

    /** Returns the minimum value included in the interval. */
    public long getMin() {
        return min;
    }

    /** Returns the maximum value included in the interval. */
    public long getMax() {
        return max;
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

    /** Returns an interval representing the multiplication of the
     * given interval with this one.
     * @param other interval to multiply by this one
     * @return product
     */
    public DiscreteInterval times(DiscreteInterval other) {
        long terms[] = { min * other.min,
                min * other.max,
                max * other.min,
                max * other.max
        };
        long newMin = terms[0];
        long newMax = terms[0];
        for(int i=1; i<terms.length; i++) {
            if (terms[i] < newMin) newMin = terms[i];
            if (terms[i] > newMax) newMax = terms[i];
        }
        return new DiscreteInterval(newMin, newMax);
    }

    /** Returns an interval representing the division of this
     * interval by the given one
     * @param other interval to divide this one by
     * @return quotient
     */
    public DiscreteInterval dividedBy(DiscreteInterval other) {
        long terms[] = { min / other.min,
                min / other.max,
                max / other.min,
                max / other.max
        };
        long newMin = terms[0];
        long newMax = terms[0];
        for(int i=1; i<terms.length; i++) {
            if (terms[i] < newMin) newMin = terms[i];
            if (terms[i] > newMax) newMax = terms[i];
        }
        return new DiscreteInterval(newMin, newMax);
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
    
    /** Determines whether the given interval is completely
     * contained by this one.
     */
    public boolean contains(DiscreteInterval other) {
        return (min <= other.getMin() && max >= other.getMax());
    }

    /** Determines whether this interval is strictly greater
     * than the given one. In other words, all of the possible
     * values represented by this interval are strictly 
     * greater than all the possible values represented by
     * the argument.
     * @param other interval for comparison
     * @return boolean
     */
    public boolean greaterThan(DiscreteInterval other) {
        return min > other.max;
    }

    /** Determines whether this interval is strictly less
     * than the given one. In other words, all of the possible
     * values represented by this interval are strictly 
     * less than all the possible values represented by
     * the argument.
     * @param other interval for comparison
     * @return boolean
     */
    public boolean lessThan(DiscreteInterval other) {
        return max < other.min;
    }

    @Override
    public String toString() {
        return "[" + min + "," + max + "]";
    }
    
}
