/* Copyright 2009-2012 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.fishwife.jrugged.PerformanceMonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Aspect that wraps methods annotated with {@link org.fishwife.jrugged.PerformanceMonitor} with a
 * {@link org.fishwife.jrugged.PerformanceMonitor}.  The value given to the
 * PerformanceMonitor annotation serves as a key for a PerformanceMonitor
 * instance.  Thus it is possible to have a PerformanceMonitor per method.
 * Alternatively, PerformanceMonitor can be shared across methods and classes by
 * using the same value for the monitor key.
 */
@Aspect
@DeclarePrecedence("PerformanceMonitorAspect, CircuitBreakerAspect")
public class PerformanceMonitorAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(PerformanceMonitorAspect.class);

    private volatile PerformanceMonitorFactory performanceMonitorFactory;

    /** Default constructor. */
    public PerformanceMonitorAspect() {
        performanceMonitorFactory = new PerformanceMonitorFactory();
    }

    /**
     * Sets the {@link org.fishwife.jrugged.PerformanceMonitorFactory} to use when creating new
     * {@link org.fishwife.jrugged.PerformanceMonitor} instances.
     * @param performanceMonitorFactory the {@link org.fishwife.jrugged.PerformanceMonitorFactory} to
     *   use.
     */
    public void setPerformanceMonitorFactory(
            PerformanceMonitorFactory performanceMonitorFactory) {
        this.performanceMonitorFactory = performanceMonitorFactory;
    }


    /**
     * Get the {@link org.fishwife.jrugged.PerformanceMonitorFactory} that is being used to create
     * new {@link org.fishwife.jrugged.PerformanceMonitor} instances.
     * @return the {@link org.fishwife.jrugged.PerformanceMonitorFactory}.
     */
    public PerformanceMonitorFactory getPerformanceMonitorFactory() {
        return performanceMonitorFactory;
    }

    /**
     * Wraps a method annotated with the {@link org.fishwife.jrugged.PerformanceMonitor} annotation
     * with a {@link org.fishwife.jrugged.PerformanceMonitor}.
     * 
     * @param pjp Represents the method that is being executed.
     * @param performanceMonitorAnnotation The PerformanceMonitor annotation
     * associated with the method being execute.
     * @return Value returned by the method that is being wrapped.
     * @throws Throwable Whatever the wrapped method throws will be thrown by
     * this method.
     */
    @Around("@annotation(performanceMonitorAnnotation)")
    public Object monitor(final ProceedingJoinPoint pjp,
            PerformanceMonitor performanceMonitorAnnotation) throws Throwable {
        String monitorName = performanceMonitorAnnotation.value();

        if (logger.isDebugEnabled()) {
            logger.debug("Have @PerformanceMonitor method with monitor name {}, " +
                    "wrapping call on method {} of target object {}",
                    new Object[]{
                            monitorName,
                            pjp.getSignature().getName(),
                            pjp.getTarget()});
        }

        org.fishwife.jrugged.PerformanceMonitor performanceMonitor =
                performanceMonitorFactory.findPerformanceMonitor(
                            monitorName);

        if (performanceMonitor == null) {
            performanceMonitor =
                    performanceMonitorFactory.createPerformanceMonitor(
                            monitorName);
        }

        return performanceMonitor.invoke(
                new Callable<Object>() {
                    public Object call() throws Exception {
                        Object retval;
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
    }
}
