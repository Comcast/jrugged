package org.fishwife.jrugged.interval;

public class DiscreteInterval {

    private long min;
    private long max;
    
    public DiscreteInterval(long min, long max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public DiscreteInterval plus(DiscreteInterval other) {
        return new DiscreteInterval(this.min + other.min, this.max + other.max);
    }

    public DiscreteInterval negate() {
        return new DiscreteInterval(-1 * this.max, -1 * this.min);
    }

    public DiscreteInterval minus(DiscreteInterval other) {
        return new DiscreteInterval(this.min - other.max, this.max - other.min);
    }

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
    
    @Override
    public String toString() {
        return "[" + min + "," + max + "]";
    }

    public boolean contains(DiscreteInterval other) {
        return (min <= other.getMin() && max >= other.getMax());
    }

    public boolean greaterThan(DiscreteInterval other) {
        return min > other.max;
    }

    public boolean lessThan(DiscreteInterval other) {
        return max < other.min;
    }

}
