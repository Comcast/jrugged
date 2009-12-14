package org.fishwife.jrugged.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an ExceptionCircuit.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionCircuit {

    /**
     * Name of the circuit.  Each annotation with a shared name shares the same
     * CircuitBreaker.
     */
    String name();

    /**
     * Exception type which trips the CircuitBreaker.
     */
    Class<? extends Exception> kind() default Exception.class;

    /**
     * Number of exceptions which must occur in <code>period</code> to trip
     * the circuit
     */
    int frequency() default -1;

    /**
     * Period of time in which <code>frequency</code> errors must occur to
     * trip the circuit.
     */
    long period() default -1;

    /**
     * Amount of time after which the circuit is reset into an open state.
     */
    long reset() default -1;

}