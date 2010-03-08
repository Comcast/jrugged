package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerExceptionMapper;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/** This is basically a {@link CircuitBreaker} that adds JMX
 * annotations to some of the methods so that the core library
 * doesn't have to depend on spring-context.
 */
@ManagedResource
public class CircuitBreakerBean extends CircuitBreaker {
    public CircuitBreakerBean() { super(); }

    public CircuitBreakerBean(FailureInterpreter fi) {
        super.setFailureInterpreter(fi);
    }

    public CircuitBreakerBean(CircuitBreakerExceptionMapper mapper) {
        super.setExceptionMapper(mapper);
    }

    public CircuitBreakerBean(FailureInterpreter fi, CircuitBreakerExceptionMapper mapper) {
        super.setFailureInterpreter(fi);
        super.setExceptionMapper(mapper);
    }

    @ManagedOperation
    @Override
    public void tripHard() {
        super.tripHard();
    }

    @ManagedAttribute
    @Override
    public long getLastTripTime() {
        return super.getLastTripTime();
    }

    @ManagedAttribute
    @Override
    public long getTripCount() {
        return super.getTripCount();
    }

    @ManagedOperation
    @Override
    public void reset() {
        super.reset();
    }

    @ManagedAttribute
    @Override
    public long getResetMillis() {
        return super.getResetMillis();
    }

    @ManagedOperation
    @Override
    public void setResetMillis(long l) {
        super.setResetMillis(l);
    }

    @ManagedAttribute
    @Override
    public String getHealthCheck() { return super.getStatus().getSignal(); }

    @ManagedOperation
    @Override
    public void setLimit(int limit) {
        ((DefaultFailureInterpreter) super.getFailureInterpreter()).setLimit(limit);
    }

    @ManagedOperation
    @Override
    public void setWindowMillis(long windowMillis) {
        super.setWindowMillis(windowMillis);
    }
}
