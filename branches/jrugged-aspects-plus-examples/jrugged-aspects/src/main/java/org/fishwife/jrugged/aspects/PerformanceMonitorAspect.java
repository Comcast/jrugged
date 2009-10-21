package org.fishwife.jrugged.aspects;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.fishwife.jrugged.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect that wraps methods annotated with {@link Monitorable} with a
 * {@link PerformanceMonitor}.  The value given to the Monitorable annotation
 * serves as a key for a PerformanceMonitor instance.  Thus it is possible to
 * have a PerformanceMonitor per method.  Alternatively, a PerformanceMonitor
 * can be shared across methods and classes by using the same value for the
 * monitor key.
 * 
 * @author bschmaus
 *
 */
@Aspect
public class PerformanceMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorAspect.class);

    private int updateIntervalInSeconds = 5;
    public int getUpdateIntervalInSeconds() {
        return updateIntervalInSeconds;
    }
    public void setUpdateIntervalInSeconds(int updateIntervalInSeconds) {
        this.updateIntervalInSeconds = updateIntervalInSeconds;
    }

    private Map<String, PerformanceMonitor> monitors = new HashMap<String, PerformanceMonitor>();
    public Map<String, PerformanceMonitor> getMonitors() {
        return monitors;
    }
    /**
     * Wraps a method annotated with the {@link Monitorable} annotation
     * with a {@link PerformanceMonitor}.
     * 
     * @param pjp Represents the method that is being executed.
     * @param monitorable The Monitorable annotation associated with the method
     * being execute.
     * @return Value returned by the method that is being wrapped.
     * @throws Throwable Whatever the wrapped method throws will be thrown
     * by this method.
     */
    @Around("@annotation(monitorable)")
    public Object monitor(final ProceedingJoinPoint pjp, Monitorable monitorable) throws Throwable {
        String monitorName = monitorable.value();
        logger.debug("Have monitorable method with monitor name {}, wrapping call on method {} of target object {}", new Object[] { monitorName, pjp.getSignature().getName(), pjp.getTarget() });
        PerformanceMonitor performanceMonitor = monitors.get(monitorName);
        if (performanceMonitor == null) {
            performanceMonitor = new PerformanceMonitor(updateIntervalInSeconds);
            monitors.put(monitorName, performanceMonitor);
            logger.debug("Initialized new performance monitor for named monitor {}", monitorName);
        }
        Object retval = performanceMonitor.invoke(
            new Callable<Object>() {
                public Object call() throws Exception {
                    Object retval = null;
                    try {
                        retval = pjp.proceed();
                    } catch (Throwable e) {
                        if (e instanceof Exception) {
                            throw (Exception) e;
                        } else {
                            throw (Error) e;
                        }
                    }
                    return retval;
                }
            }
        );
        return retval;
    }

    /**
     * Gets a performance monitor instance for the given monitor name (ie, key).
     * 
     * @param key The value of a {@link Monitorable} annotation that will be
     * used to lookup a corresponding {@link PerformanceMonitor} instance.
     * @return The PerformanceMonitor for the given key or null if there is
     * no monitor for the given key.
     */
    public PerformanceMonitor getMonitor(String key) {
        return monitors.get(key);
    }
    
}
