/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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

import org.fishwife.jrugged.SkepticBreaker;
import org.fishwife.jrugged.SkepticBreakerConfig;
import org.fishwife.jrugged.BreakerFactory;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Factory to create new {@link SkepticBreakerBean} instances and keep track of
 * them. If a {@link MBeanExportOperations} is set, then the SkepticBreakerBean will be
 * automatically exported as a JMX MBean.
 */
public class SkepticBreakerBeanFactory extends BreakerFactory implements InitializingBean {

    @Autowired(required = false)
    private MBeanExportOperations mBeanExportOperations;

    private String packageScanBase;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() {
        buildAnnotatedSkepticBreakers();
    }

    /**
     * Set the {@link MBeanExporter} to use to export {@link SkepticBreakerBean}
     * instances as JMX MBeans.
     * @param mBeanExporter the {@link MBeanExporter} to set.
     */
    @Deprecated
    public void setMBeanExporter(MBeanExporter mBeanExporter) {
        setMBeanExportOperations(mBeanExporter);
    }

    /**
     * Set the {@link MBeanExportOperations} to use to export {@link SkepticBreakerBean}
     * instances as JMX MBeans.
     * @param mBeanExportOperations the {@link MBeanExportOperations} to set.
     */
    public void setMBeanExportOperations(MBeanExportOperations mBeanExportOperations) {
        this.mBeanExportOperations = mBeanExportOperations;
    }

    /**
     * If specified, SkepticBreakerBeanFactory will scan all classes
     * under packageScanBase for methods with the
     * {@link org.fishwife.jrugged.aspects.SkepticBreaker} annotation
     * and initialize skepticbreakers for them.
     *
     * @param packageScanBase Where should the scan for annotations begin
     */
    public void setPackageScanBase(String packageScanBase) {
        this.packageScanBase = packageScanBase;
    }

    /**
     * If packageScanBase is defined will search packages for {@link org.fishwife.jrugged.aspects.SkepticBreaker}
     * annotations and create skepticbreakers for them.
     */
    public void buildAnnotatedSkepticBreakers() {
        if (packageScanBase != null) {
            AnnotatedMethodScanner methodScanner = new AnnotatedMethodScanner();
            for (Method m : methodScanner.findAnnotatedMethods(packageScanBase, org.fishwife.jrugged.aspects.SkepticBreaker.class)) {
                org.fishwife.jrugged.aspects.SkepticBreaker skepticBreakerAnnotation = m.getAnnotation(org.fishwife.jrugged.aspects.SkepticBreaker.class);
                DefaultFailureInterpreter dfi = new DefaultFailureInterpreter(skepticBreakerAnnotation.ignore(), skepticBreakerAnnotation.limit(), skepticBreakerAnnotation.windowMillis());
                SkepticBreakerConfig config = new SkepticBreakerConfig(skepticBreakerAnnotation.goodBase(), skepticBreakerAnnotation.goodMult(), skepticBreakerAnnotation.waitBase(), 
                		skepticBreakerAnnotation.waitMult(), skepticBreakerAnnotation.maxLevel(), dfi);
                createSkepticBreaker(skepticBreakerAnnotation.name(), config);
            }
        }

    }

    /**
     * Create a new {@link SkepticBreakerBean} and map it to the provided value.
     * If the {@link MBeanExportOperations} is set, then the SkepticBreakerBean will be
     * exported as a JMX MBean.
     * If the SkepticBreaker already exists, then the existing instance is
     * returned.
     * @param name   the value for the {@link org.fishwife.jrugged.SkepticBreaker}
     * @param config the {@link org.fishwife.jrugged.SkepticBreakerConfig}
     */
    public synchronized SkepticBreaker createSkepticBreaker(String name, SkepticBreakerConfig config) {

        SkepticBreaker skepticBreaker = findSkepticBreaker(name);

        if (skepticBreaker == null) {
            skepticBreaker = new SkepticBreakerBean(name);

            configureSkepticBreaker(name, skepticBreaker, config);

            if (mBeanExportOperations != null) {
                ObjectName objectName;

                try {
                    objectName = new ObjectName("org.fishwife.jrugged.spring:type=SkepticBreakerBean," + "name=" + name);
                } catch (MalformedObjectNameException e) {
                    throw new IllegalArgumentException("Invalid MBean Name " + name, e);

                }

                mBeanExportOperations.registerManagedResource(skepticBreaker, objectName);
            }

            addSkepticBreakerToMap(name, skepticBreaker);
        }

        return skepticBreaker;
    }

    /**
     * Find an existing {@link SkepticBreakerBean}
     * @param name the value for the {@link SkepticBreakerBean}
     * @return the found {@link SkepticBreakerBean}, or null if it is not found.
     */
    public SkepticBreakerBean findSkepticBreakerBean(String name) {
        SkepticBreaker skepticBreaker = findSkepticBreaker(name);

        if (skepticBreaker instanceof SkepticBreakerBean) {
            return (SkepticBreakerBean) skepticBreaker;
        }
        return null;
    }
}
