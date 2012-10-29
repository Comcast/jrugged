package org.fishwife.jrugged;

/**
 * Thrown to indicate that an exception occurred, but the circuit should stay
 * open.
 * 
 * @see CircuitBreaker
 */
public class CircuitShouldStayOpenException extends RuntimeException {

    public CircuitShouldStayOpenException(Throwable cause) {
        super(cause);
    }

    public CircuitShouldStayOpenException(String message, Throwable cause) {
        super(message, cause);
    }

}
