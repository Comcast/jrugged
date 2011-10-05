package org.fishwife.jrugged.clocks;

import org.fishwife.jrugged.interval.DiscreteInterval;

public class Timer {

    private HardwareClock clock;
    private boolean wasSet = false;
    private DiscreteInterval targetElapsedTime;
    private DiscreteInterval startTime;
    private DiscreteInterval targetEndTime;
    
    public Timer() {
        this(new DefaultHardwareClock());
    }
    
    public Timer(HardwareClock clock) {
        this.clock = clock;
    }

    public boolean set(long duration, long error) {
        if (error < clock.getGranularity()) return false;
        wasSet = true;
        targetElapsedTime = new DiscreteInterval(duration - error, duration + error);
        return true;
    }

    public void start() {
        if (!wasSet) throw new IllegalStateException("cannot start a timer that has not been set");
        startTime = clock.getNanoTime();
        targetEndTime = startTime.plus(targetElapsedTime);
    }

    public boolean hasElapsed() {
        if (startTime == null) return false;
        return (clock.getNanoTime().getMin() >= targetEndTime.getMin());
    }

    public boolean isLate() {
        if (startTime == null) return false;
        return (clock.getNanoTime().getMax() > targetEndTime.getMax());
    }

    public DiscreteInterval getTimeRemaining() {
        if (!wasSet) throw new IllegalStateException("cannot compute time remaining without having duration set");
        if (startTime == null) return targetElapsedTime;
        DiscreteInterval diff = targetEndTime.minus(clock.getNanoTime());
        return (diff.getMin() <= 0L) ? new DiscreteInterval(0L,0L) : diff; 
    }

    public void waitUntilElapsed() {
        if (!wasSet) throw new IllegalStateException("cannot wait until duration has been set");
        while(!hasElapsed()) /*spin*/;
    }

}
