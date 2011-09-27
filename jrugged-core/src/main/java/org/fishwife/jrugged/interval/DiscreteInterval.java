package org.fishwife.jrugged.interval;

class DiscreteInterval {

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

}
