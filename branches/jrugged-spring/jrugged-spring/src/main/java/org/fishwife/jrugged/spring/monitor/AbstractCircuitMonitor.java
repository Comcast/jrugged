package org.fishwife.jrugged.spring.monitor;

import org.fishwife.jrugged.circuit.CircuitBreaker;
import org.fishwife.jrugged.ExceptionFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.fishwife.jrugged.spring.circuit.ExceptionCircuitInterceptor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import java.util.Date;

/**
 * Defines JMX attributes for monitorable circuit.
 *
 * @see CircuitBreaker
 */
public abstract class AbstractCircuitMonitor {

    private ExceptionCircuitInterceptor circuitInterceptor;

    protected abstract String circuitName();

    @ManagedOperation
    public void open() {
        this.circuit().reset();
    }

    @ManagedOperation
    public void close() {
        this.circuit().tripHard();
    }

    @ManagedAttribute
    public String getStatus() {
        return this.circuit().getStatus().toString();
    }

    @ManagedAttribute
    public String getLastFailureTime() {
        return new Date(this.circuit().getLastFailureTime()).toString();
    }

    @ManagedAttribute
    public String getOpenBreakerCount() {
        return new Long(this.circuit().getOpenBreakerCount()).toString();
    }

    @ManagedAttribute
    public int getFrequency() {
        return this.interpreter().getFrequency();
    }

    @ManagedAttribute
    public void setFrequency(int frequency) {
        this.interpreter().setFrequency(frequency);
    }

    @ManagedAttribute
    public long getTime() {
        return this.interpreter().getTime();
    }

    @ManagedAttribute
    public void setTime(long time) {
        this.interpreter().setTime(time);
    }

    @ManagedAttribute
    public long getResetTimeout() {
        return this.circuit().getResetMillis();
    }

    @ManagedAttribute
    public void setResetTimeout(long reset) {
        this.circuit().setResetMillis(reset);
    }

    private ExceptionFailureInterpreter interpreter() {
        final FailureInterpreter interpreter = this.circuit()
                .getFailureInterpreter();
        if (!(interpreter instanceof ExceptionFailureInterpreter))
            throw new IllegalStateException(
                    "misconfigured circuit; no ExceptionFailureInterpreter available.");
        return (ExceptionFailureInterpreter) interpreter;
    }

    private CircuitBreaker circuit() {
        final String name = this.circuitName();
        if (!this.circuitInterceptor.hasCircuitBreaker(name))
            throw new IllegalStateException(String.format(
                    "circuit '%s' not found", name));
        return this.circuitInterceptor.getCircuitBreaker(name);
    }

    public void setMonitorAspect(ExceptionCircuitInterceptor circuitInterceptor) {
        this.circuitInterceptor = circuitInterceptor;
    }

}
