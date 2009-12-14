package org.fishwife.jrugged;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Fails if exception frequences surpasses a declared number per time 
 * threshhold.
 */
public final class ExceptionFailureInterpreter implements FailureInterpreter {

    private Class<? extends Exception> kind;
    private int frequency;
    private long time;
    private TimeUnit unit;
    // tracks times when exceptions occurred
    private List<Long> errorTimes = Collections
            .synchronizedList(new LinkedList<Long>());

    public ExceptionFailureInterpreter(Class<? extends Exception> kind) {
        this.kind = kind;
    }
    
    public ExceptionFailureInterpreter(Class<? extends Exception> kind, int frequency,
            long time, TimeUnit unit) {
        this.kind = kind;
        this.frequency = frequency;
        this.time = time;
        this.unit = unit;
    }

    private boolean hasTimeConditions() {
        return this.frequency > 0 && this.time > 0;
    }
    
    public <V> V invoke(Callable<V> c) throws Exception {
        try {
            return c.call();
        } catch (Exception e) {
            // if Exception is of specified type, and time conditions exist,
            // keep circuit open unless exception threshold has passed
            
            // if Exception if of specified type, and no time conditions exist
            // close circuit
            
            // if Exception is not of specified type, keep circuit open
            
            final boolean isInstance = kind.isInstance(e);
            final boolean hasTimeConditions = this.hasTimeConditions();
            if (isInstance && hasTimeConditions) {
                errorTimes.add(System.currentTimeMillis());

                // calculates time for which we remove any errors before
                final long removeTimesBeforeMillis = System.currentTimeMillis()
                        - this.unit.toMillis(this.time);

                // removes errors before cutoff 
                // (could we speed this up by using binary search to find the entry point,
                // then removing any items before that point?)
                for (final Iterator<Long> i = this.errorTimes.iterator(); i
                        .hasNext();) {
                    final Long time = i.next();
                    if (time < removeTimesBeforeMillis) {
                        i.remove();
                    } else {
                        // the list is sorted by time, if I didn't remove this item
                        // I won't be remove any after it either
                        break;
                    }
                }

                // The exception count hasn't passed the limit, so the circuit
                // stays open
                if (errorTimes.size() < this.frequency)
                    throw new CircuitShouldStayOpenException(e);
                else
                    throw e;
            } else if (isInstance) {
                throw e;
            } else {
                throw new CircuitShouldStayOpenException(e);
            }
        }
    }
    
    /**
     * get the value of kind
     * @return the value of kind
     */
    public Class<? extends Exception> getKind(){
        return this.kind;
    }
    /**
     * set a new value to errorCount
     * @param errorCount the new value to be used
     */
    public <K extends Exception> void setKind(Class<K> kind) {
        this.kind = kind;
    }

    /**
     * get the value of frequency
     * @return the value of frequency
     */
    public int getFrequency(){
        return this.frequency;
    }
    /**
     * set a new value to errorCount
     * @param errorCount the new value to be used
     */
    public void setFrequency(int frequency) {
        this.frequency=frequency;
    }

    /**
     * get the value of time
     * @return the value of time
     */
    public long getTime(){
        return this.time;
    }
    /**
     * set a new value to time
     * @param time the new value to be used
     */
    public void setTime(long time) {
        this.time=time;
    }

    /**
     * get the value of unit
     * @return the value of unit
     */
    public TimeUnit getUnit(){
        return this.unit;
    }
    /**
     * set a new value to unit
     * @param unit the new value to be used
     */
    public void setUnit(TimeUnit unit) {
        this.unit=unit;
    }
}

