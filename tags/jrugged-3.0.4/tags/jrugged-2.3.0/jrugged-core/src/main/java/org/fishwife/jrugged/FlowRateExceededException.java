package org.fishwife.jrugged;

public class FlowRateExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    private double rate = 0;
    private double maximumRate = 0;

    public FlowRateExceededException() {
    }

    public FlowRateExceededException(String message, double rate, double maximumRate) {
        super(message);
        this.rate = rate;
        this.maximumRate = maximumRate;
    }

    public FlowRateExceededException(double rate, double maximumRate) {
        this("Rate Exceeded (Max=" + maximumRate + ", Current=" + rate + ")", rate, maximumRate);
    }
}
