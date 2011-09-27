package org.fishwife.jrugged.interval;

class RealInterval {

    private double min;
    private double max;
    
    public RealInterval(double min, double max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public RealInterval plus(RealInterval other) {
        return new RealInterval(this.min + other.min, this.max + other.max);
    }

    public RealInterval negate() {
        return new RealInterval(-1 * this.max, -1 * this.min);
    }

    public RealInterval minus(RealInterval other) {
        return new RealInterval(this.min - other.max, this.max - other.min);
    }

    public RealInterval times(RealInterval other) {
        double terms[] = { min * other.min,
                min * other.max,
                max * other.min,
                max * other.max
        };
        double newMin = terms[0];
        double newMax = terms[0];
        for(int i=1; i<terms.length; i++) {
            if (terms[i] < newMin) newMin = terms[i];
            if (terms[i] > newMax) newMax = terms[i];
        }
        return new RealInterval(newMin, newMax);
    }

    public RealInterval dividedBy(RealInterval other) {
        double terms[] = { min / other.min,
                min / other.max,
                max / other.min,
                max / other.max
        };
        double newMin = terms[0];
        double newMax = terms[0];
        for(int i=1; i<terms.length; i++) {
            if (terms[i] < newMin) newMin = terms[i];
            if (terms[i] > newMax) newMax = terms[i];
        }
        return new RealInterval(newMin, newMax);
    }

}
