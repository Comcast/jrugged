package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.RequestCounter;
import org.springframework.jmx.export.annotation.ManagedOperation;

public class RequestCounterBean extends RequestCounter {
    public RequestCounterBean() {
        super();
    }

    @ManagedOperation
    @Override
    public synchronized long[] sample() {
        return super.sample();
    }

}
