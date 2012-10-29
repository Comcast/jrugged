package org.fishwife.jrugged.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.fishwife.jrugged.CircuitBreakerException;

public interface CircuitBreakerExceptionMapper<T extends Exception> {

    public T map(ProceedingJoinPoint pjp, ExceptionCircuit circuitConfig, CircuitBreakerException e);
    
}
