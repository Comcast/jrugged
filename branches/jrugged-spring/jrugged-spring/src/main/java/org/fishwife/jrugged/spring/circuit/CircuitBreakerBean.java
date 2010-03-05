package org.fishwife.jrugged.spring;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerExceptionMapper;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.fishwife.jrugged.Status;

/** Convenience class for configuring a {@link CircuitBreaker} with
 *  common customization properties.
 * <p>
 * For example:
 * <pre>
 * &lt;bean id="myCircuitBreaker" class="org.fishwife.jrugged.spring.circuit.CircuitBreakerBean"&gt;
 *    &lt;property name="limit" value="5"/&gt;
 *    &lt;property name="windowMillis" value="2000"/&gt;
 *    &lt;property name="resetMillis" value="10000"/&gt;
 *    &lt;property name="ignore"&gt;
 *       &lt;list&gt;
 *         &lt;value&gt;java.io.FileNotFoundException&lt;/value&gt;
 *         &lt;value&gt;com.myapp.MyInternalAcceptableException&lt;/value&gt;
 *       &lt;/list&gt;
 *     &lt;/property&gt;
 *   &lt;/bean&gt;
 * </pre>
 */
public class CircuitBreakerBean extends CircuitBreaker {

	private CircuitBreaker breaker = new CircuitBreaker();

	/** Specifies the failure tolerance limit for the {@link
	 *  DefaultFailureInterpreter} that comes with a {@link
	 *  CircuitBreaker} by default.
	 *  @see {@link DefaultFailureInterpreter}
	 *  @param limit the number of tolerated failures in a window
	 */
	public void setLimit(int limit) {
		((DefaultFailureInterpreter)breaker.getFailureInterpreter())
			.setLimit(limit);
	}

	/** Specifies the tolerance window in milliseconds for the {@link
	 *  DefaultFailureInterpreter} that comes with a {@link
	 *  CircuitBreaker} by default.
	 *  @see {@link DefaultFailureInterpreter}
	 *  @param windowMillis length of the window in milliseconds
	 */
	public void setWindowMillis(long windowMillis) {
		DefaultFailureInterpreter dfi = 
			((DefaultFailureInterpreter)breaker.getFailureInterpreter());
		dfi.setWindowMillis(windowMillis);
	}

	public void setResetMillis(long resetMillis) {
		breaker.setResetMillis(resetMillis);
	}

	/** Specifies a set of {@link Throwable} classes that should not
	 *  be considered failures by the {@link CircuitBreaker}.
	 *  @see {@link DefaultFailureInterpreter}
	 *  @param ignore a {@link Collection} of {@link Throwable}
	 *  classes
	 */
	public void setIgnore(Collection<Class<? extends Throwable>> ignore) {
		Class[] classes = new Class[ignore.size()];
		int i = 0;
		for(Class c : ignore) {
			classes[i] = c;
			i++;
		}
		DefaultFailureInterpreter dfi = 
			((DefaultFailureInterpreter)breaker.getFailureInterpreter());
		dfi.setIgnore(classes);
	}

	@Override
    public <V> V invoke(Callable<V> c) throws Exception {
		return breaker.invoke(c);
	}

	@Override
    public void invoke(Runnable r) throws Exception {
		breaker.invoke(r);
	}
	
	@Override
    public <V> V invoke(Runnable r, V result) throws Exception {
		return breaker.invoke(r, result);
	}	

	@Override
    public void trip() { breaker.trip(); }

	@Override
	public void tripHard() { breaker.tripHard(); }
	
	@Override
    public long getLastTripTime() { return breaker.getLastTripTime(); }
	
	@Override
    public long getTripCount() { return breaker.getTripCount(); }

	@Override
    public void reset() { breaker.reset(); }
	
	@Override
    public Status getStatus() { return breaker.getStatus(); }

	@Override
    public long getResetMillis() { return breaker.getResetMillis(); }

	@Override
	public void setFailureInterpreter(FailureInterpreter fi) {
		breaker.setFailureInterpreter(fi);
	}

	@Override
    public FailureInterpreter getFailureInterpreter() {
		return breaker.getFailureInterpreter();
	}

	@Override
    public void setExceptionMapper(CircuitBreakerExceptionMapper mapper) {
		breaker.setExceptionMapper(mapper);
	}
}