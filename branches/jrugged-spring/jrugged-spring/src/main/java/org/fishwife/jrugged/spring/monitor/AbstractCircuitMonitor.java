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
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import java.util.Date;

/**
 * Defines JMX attributes for monitorable circuit.
 *
 * @see CircuitBreaker
 */
public abstract class AbstractCircuitMonitor {

    private CircuitBreaker circuit;

	private static String UNSUPPORTED_SETTER_MSG =
		"Setting this value is not supported because a custom FailureInterpreter has been configured for this circuit. (i.e. Current FailureInterpreter is not assignable from DefaultFailureInterpreter)";

    protected abstract String circuitName();

    @ManagedOperation
    public void open() {
        this.circuit.reset();
    }

    @ManagedOperation
    public void close() {
        this.circuit.tripHard();
    }

    @ManagedAttribute
    public String getStatus() {
        return this.circuit.getStatus().toString();
    }

    @ManagedAttribute
    public String getLastFailureTime() {
        return new Date(this.circuit.getLastTripTime()).toString();
    }

    @ManagedAttribute
    public String getOpenBreakerCount() {
        return Long.toString(this.circuit.getTripCount());
    }

    @ManagedAttribute
    public int getLimit() {
		FailureInterpreter fi = circuit.getFailureInterpreter();
		if (fi == null || !(fi instanceof DefaultFailureInterpreter)) {
			return -1;
		}
		return ((DefaultFailureInterpreter)fi).getLimit();
    }

    @ManagedAttribute
    public void setLimit(int limit) {
		FailureInterpreter fi = circuit.getFailureInterpreter();
		if (fi == null || !(fi instanceof DefaultFailureInterpreter)) {
			throw new IllegalStateException(UNSUPPORTED_SETTER_MSG);
		}
		((DefaultFailureInterpreter)fi).setLimit(limit);
    }

    @ManagedAttribute
    public long getWindowMillis() {
		FailureInterpreter fi = circuit.getFailureInterpreter();
		if (fi == null || !(fi instanceof DefaultFailureInterpreter)) {
			return -1;
		}
		return ((DefaultFailureInterpreter)fi).getWindowMillis();
    }

    @ManagedAttribute
    public void setWindowMillis(long windowMillis) {
		FailureInterpreter fi = circuit.getFailureInterpreter();
		if (fi == null || !(fi instanceof DefaultFailureInterpreter)) {
			throw new IllegalStateException(UNSUPPORTED_SETTER_MSG);
		}
		((DefaultFailureInterpreter)fi).setWindowMillis(windowMillis);
    }

    @ManagedAttribute
    public long getResetMillis() {
        return this.circuit.getResetMillis();
    }

    @ManagedAttribute
    public void setResetMillis(long reset) {
        this.circuit.setResetMillis(reset);
    }

	public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
		this.circuit = circuitBreaker;
	}
	
	public CircuitBreaker getCircuitBreaker() {
		return this.circuit;
	}
	
}
