/* Copyright 2009 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged.spring.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInvocation;
import org.fishwife.jrugged.PerformanceMonitor;
import org.fishwife.jrugged.spring.BaseJruggedInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that wraps methods configured in spring with a
 * {@link PerformanceMonitor}.  The value given to the config map of monitors
 * serves as a key for a PerformanceMonitor instance.  Thus it is possible to
 * have a PerformanceMonitor per method.  Alternatively, a PerformanceMonitor
 * can be shared across methods and classes by using the same value for the
 * monitor key.
 */
public class PerformanceMonitorInterceptor extends BaseJruggedInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorInterceptor.class);

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

    public void setMonitors(Map<String, PerformanceMonitor> monitors) {
        this.monitors = monitors;
    }

    /**
     * Gets a performance monitor instance for the given monitor name (ie, key).
     *
     * @param key The string name of a method that will be
     * used to lookup a corresponding {@link PerformanceMonitor} instance.
     * @return The PerformanceMonitor for the given key or null if there is
     * no monitor for the given key.
     */
    public PerformanceMonitor getMonitor(String key) {
        return monitors.get(key);
    }

    /**
     * Wraps a method indicated in spring config
     * with a {@link PerformanceMonitor}.
     *
     * @param invocation Represents the method that is being executed.
     * @return Value returned by the method that is being wrapped.
     * @throws Throwable Whatever the wrapped method throws will be thrown
     * by this method.
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String monitorName = (getInvocationTraceName(invocation).split("\\."))[1];
        
        //logger.debug("Have monitorable method with monitor name {}, wrapping call on method {} of target object {}",
        //        new Object[] { monitorName, invocation.getSignature().getName(), invocation.getTarget() });
        PerformanceMonitor performanceMonitor = monitors.get(monitorName);

        if (performanceMonitor == null) {
            performanceMonitor = new PerformanceMonitor(updateIntervalInSeconds);
            monitors.put(monitorName, performanceMonitor);
            logger.debug("Initialized new performance monitor for named monitor {}", monitorName);
        }

        return performanceMonitor.invoke(
            new Callable<Object>() {
                public Object call() throws Exception {
                    Object retval;
                    try {
                        retval = invocation.proceed();
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

    public void afterPropertiesSet() throws Exception {
        // No - Op.
    }
}
