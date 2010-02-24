/* CircuitBreaker.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fishwife.jrugged.spring.monitor;

import org.fishwife.jrugged.PerformanceMonitor;
import org.fishwife.jrugged.spring.monitor.PerformanceMonitorInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Abstract class that can be used as the basis for an MBean that exposes
 * data from an underlying {@link PerformanceMonitor}.
 */
public abstract class AbstractMonitor implements InitializingBean {

    protected PerformanceMonitorInterceptor monitorInterceptor;
    protected DelegatingPerformanceMonitor delegatingMonitor;

    public void setMonitorInterceptor(PerformanceMonitorInterceptor a) {
        this.monitorInterceptor = a;
    }

    public void setDelegatingMonitor(DelegatingPerformanceMonitor delegatingMonitor) {
        this.delegatingMonitor = delegatingMonitor;
    }

    /**
     *
     * @return The {@link PerformanceMonitor} associated with this Monitor MBean.
     */
    protected abstract PerformanceMonitor getMonitor();

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.monitorInterceptor, "must specify a [monitorInterceptor]");
    }

}
