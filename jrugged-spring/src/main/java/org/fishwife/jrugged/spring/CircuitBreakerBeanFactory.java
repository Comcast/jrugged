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
package org.fishwife.jrugged.spring;

import java.lang.reflect.Method;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerConfig;
import org.fishwife.jrugged.CircuitBreakerFactory;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Factory to create new {@link CircuitBreakerBean} instances and keep track of
 * them. If a {@link MBeanExportOperations} is set, then the CircuitBreakerBean will be
 * automatically exported as a JMX MBean.
 */
public class CircuitBreakerBeanFactory extends CircuitBreakerFactory implements InitializingBean {

    @Autowired(required = false)
    private MBeanExportOperations mBeanExportOperations;

    private String packageScanBase;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() {
        buildAnnotatedCircuitBreakers();
    }
    
    /**
     * Set the {@link MBeanExporter} to use to export {@link CircuitBreakerBean}
     * instances as JMX MBeans.
     * @param mBeanExporter the {@link MBeanExporter} to set.
     */
    @Deprecated
    public void setMBeanExporter(MBeanExporter mBeanExporter) {
        setMBeanExportOperations(mBeanExporter);
    }

    /**
     * Set the {@link MBeanExportOperations} to use to export {@link CircuitBreakerBean}
     * instances as JMX MBeans.
     * @param mBeanExportOperations the {@link MBeanExportOperations} to set.
     */
    public void setMBeanExportOperations(MBeanExportOperations mBeanExportOperations) {
        this.mBeanExportOperations = mBeanExportOperations;
    }

    /**
     * If specified, CircuitBreakerBeanFactory will scan all classes
     * under packageScanBase for methods with the
     * {@link org.fishwife.jrugged.aspects.CircuitBreaker} annotation
     * and initialize circuitbreakers for them.
     *
     * @param packageScanBase Where should the scan for annotations begin
     */
    public void setPackageScanBase(String packageScanBase) {
        this.packageScanBase = packageScanBase;
    }

    /**
     * If packageScanBase is defined will search packages for {@link org.fishwife.jrugged.aspects.CircuitBreaker}
     * annotations and create circuitbreakers for them.
     */
    public void buildAnnotatedCircuitBreakers() {
        if (packageScanBase != null) {
            AnnotatedMethodScanner methodScanner = new AnnotatedMethodScanner();
            for (Method m : methodScanner.findAnnotatedMethods(packageScanBase, org.fishwife.jrugged.aspects.CircuitBreaker.class)) {
                org.fishwife.jrugged.aspects.CircuitBreaker circuitBreakerAnnotation = m.getAnnotation(org.fishwife.jrugged.aspects.CircuitBreaker.class);
                DefaultFailureInterpreter dfi = new DefaultFailureInterpreter(circuitBreakerAnnotation.ignore(), circuitBreakerAnnotation.limit(), circuitBreakerAnnotation.windowMillis());
                CircuitBreakerConfig config = new CircuitBreakerConfig(circuitBreakerAnnotation.resetMillis(), dfi);
                createCircuitBreaker(circuitBreakerAnnotation.name(), config);
            }
        }

    }

    /**
     * Create a new {@link CircuitBreakerBean} and map it to the provided value.
     * If the {@link MBeanExportOperations} is set, then the CircuitBreakerBean will be
     * exported as a JMX MBean.
     * If the CircuitBreaker already exists, then the existing instance is
     * returned.
     * @param name   the value for the {@link org.fishwife.jrugged.CircuitBreaker}
     * @param config the {@link org.fishwife.jrugged.CircuitBreakerConfig}
     */
    public synchronized CircuitBreaker createCircuitBreaker(String name, CircuitBreakerConfig config) {

        CircuitBreaker circuitBreaker = findCircuitBreaker(name);

        if (circuitBreaker == null) {
            circuitBreaker = new CircuitBreakerBean(name);

            configureCircuitBreaker(name, circuitBreaker, config);

            if (mBeanExportOperations != null) {
                ObjectName objectName;

                try {
                    objectName = new ObjectName("org.fishwife.jrugged.spring:type=CircuitBreakerBean," + "value=" + name);
                } catch (MalformedObjectNameException e) {
                    throw new IllegalArgumentException("Invalid MBean Name " + name, e);

                }

                mBeanExportOperations.registerManagedResource(circuitBreaker, objectName);
            }

            addCircuitBreakerToMap(name, circuitBreaker);
        }

        return circuitBreaker;
    }

    /**
     * Find an existing {@link CircuitBreakerBean}
     * @param name the value for the {@link CircuitBreakerBean}
     * @return the found {@link CircuitBreakerBean}, or null if it is not found.
     */
    public CircuitBreakerBean findCircuitBreakerBean(String name) {
        CircuitBreaker circuitBreaker = findCircuitBreaker(name);

        if (circuitBreaker instanceof CircuitBreakerBean) {
            return (CircuitBreakerBean) circuitBreaker;
        }
        return null;
    }
}
