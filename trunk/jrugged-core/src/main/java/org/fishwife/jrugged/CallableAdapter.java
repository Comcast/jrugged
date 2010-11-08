package org.fishwife.jrugged;

import java.util.concurrent.Callable;

/**
 * Adapter to expose a Runnable as a Callable, with an optional result.
 * @param <R>
 */

public class CallableAdapter<R> implements Callable<R> {
    
    private Runnable runnable;
    private R result;
    
    public CallableAdapter(Runnable runnable) {
        this(runnable, null);
    }
    
    public CallableAdapter(Runnable runnable, R result) {
        this.runnable = runnable;
        this.result = result;
    }
    
    public R call() throws Exception {       
        runnable.run();
        return result;
    }
}
