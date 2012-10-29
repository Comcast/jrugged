package org.fishwife.jrugged;

import java.util.concurrent.Callable;

/**
 * Invokes provided actions and reports success or failure.
 */
public interface FailureInterpreter {

    /**
     * Invokes provided <code>Callable</code>.
     * 
     * @param c
     * @return value returned from c
     * @throws CircuitShouldStayOpenException if an exception occurred, but calling 
     * circuit should stay open.
     * @throws Exception if an unacceptable exception occurred.
     */
    <V> V invoke(Callable<V> c) throws CircuitShouldStayOpenException,
            CircuitShouldBeClosedException, Exception;

}
