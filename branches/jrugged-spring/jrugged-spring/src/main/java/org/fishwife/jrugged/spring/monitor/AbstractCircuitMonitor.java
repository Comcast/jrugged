/* Copyright 2009 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.spring.monitor;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.DefaultFailureInterpreter;
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
        return new Date(this.circuit().getLastTripTime()).toString();
    }

    @ManagedAttribute
    public String getOpenBreakerCount() {
        return Long.toString(this.circuit().getTripCount());
    }

    @ManagedAttribute
    public int getLimit() {
        return this.interpreter().getLimit();
    }

    @ManagedAttribute
    public void setLimit(int limit) {
        this.interpreter().setLimit(limit);
    }

    @ManagedAttribute
    public long getWindow() {
        return this.interpreter().getWindow();
    }

    @ManagedAttribute
    public void setWindow(long window) {
        this.interpreter().setWindow(window);
    }

    @ManagedAttribute
    public long getResetTimeout() {
        return this.circuit().getResetMillis();
    }

    @ManagedAttribute
    public void setResetTimeout(long reset) {
        this.circuit().setResetMillis(reset);
    }

    private DefaultFailureInterpreter interpreter() {
        final FailureInterpreter interpreter = this.circuit()
                .getFailureInterpreter();
        if (!(interpreter instanceof DefaultFailureInterpreter))
            throw new IllegalStateException(
                    "misconfigured circuit; no DefaultFailureInterpreter available.");
        return (DefaultFailureInterpreter) interpreter;
    }

    private CircuitBreaker circuit() {
        final String name = this.circuitName();
        if (!this.circuitInterceptor.hasCircuitBreaker(name))
            throw new IllegalStateException(String.format(
                    "circuit '%s' not found", name));
        return this.circuitInterceptor.getCircuitBreaker(name);
    }

    public void setMonitorInterceptor(ExceptionCircuitInterceptor circuitInterceptor) {
        this.circuitInterceptor = circuitInterceptor;
    }

}
