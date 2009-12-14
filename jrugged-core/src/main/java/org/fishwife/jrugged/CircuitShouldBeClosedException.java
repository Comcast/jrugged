package org.fishwife.jrugged;

/**
 * Thrown to indicate that an exception occurred and the circuit should close.
 * 
 * @see CircuitBreaker
 */
public class CircuitShouldBeClosedException extends RuntimeException {

    public CircuitShouldBeClosedException(Throwable cause) {
        super(cause);
    }

    public CircuitShouldBeClosedException(String message, Throwable cause) {
        super(message, cause);
    }

}
