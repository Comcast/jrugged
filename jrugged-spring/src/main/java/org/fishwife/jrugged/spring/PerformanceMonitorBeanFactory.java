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
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.fishwife.jrugged.PerformanceMonitor;
import org.fishwife.jrugged.PerformanceMonitorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Factory to create new {@link PerformanceMonitorBean} instances and keep track
 * of the created instances. If the {@link MBeanExportOperations} is set, then the
 * PerformanceMonitorBean will be automatically exported as a JMX MBean.
 */
public class PerformanceMonitorBeanFactory extends PerformanceMonitorFactory implements InitializingBean {

    @Autowired(required=false)
    private MBeanExportOperations mBeanExportOperations;

    private List<String> initialPerformanceMonitorList;

    private String packageScanBase;
    
    /**
     * Constructor.
     */
    public PerformanceMonitorBeanFactory() {
        initialPerformanceMonitorList = new ArrayList<String>();
        packageScanBase = null;
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() {
        createInitialPerformanceMonitors();
    }

    /**
     * Set the list of initial {@link PerformanceMonitorBean} instances to create.
     * @param initialPerformanceMonitors the list of {@link PerformanceMonitorBean} names
     */
    public void setInitialPerformanceMonitors(List<String> initialPerformanceMonitors) {
        if (initialPerformanceMonitors != null) {
            initialPerformanceMonitorList.addAll(initialPerformanceMonitors);
        }
    }

    /**
     * If specified, PerformanceMonitorBeanFactory will scan all classes
     * under packageScanBase for methods with the
     * {@link org.fishwife.jrugged.aspects.PerformanceMonitor} annotation
     * and initialize performance monitors for them.
     *
     * @param packageScanBase Where should the scan for annotations begin
     */
    public void setPackageScanBase(String packageScanBase) {
        this.packageScanBase = packageScanBase;
    }
    
    /**
     * Create the initial {@link PerformanceMonitorBean} instances.
     */
    public void createInitialPerformanceMonitors() {
        if (packageScanBase != null) {
            AnnotatedMethodScanner methodScanner = new AnnotatedMethodScanner();
            for (Method m : methodScanner.findAnnotatedMethods(packageScanBase, org.fishwife.jrugged.aspects.PerformanceMonitor.class)) {
                org.fishwife.jrugged.aspects.PerformanceMonitor performanceMonitorAnnotation = m.getAnnotation(org.fishwife.jrugged.aspects.PerformanceMonitor.class);
                initialPerformanceMonitorList.add(performanceMonitorAnnotation.value());
            }
        }
        for (String name: initialPerformanceMonitorList) {
            createPerformanceMonitor(name);
        }
    }

    /**
     * Set the {@link MBeanExporter} to use to export
     * {@link PerformanceMonitorBean} instances as JMX MBeans.
     * @param mBeanExporter the {@link MBeanExporter} to set.
     */
    @Deprecated
    public void setMBeanExporter(MBeanExporter mBeanExporter) {
        setMBeanExportOperations(mBeanExporter);
    }

    /**
     * Set the {@link MBeanExportOperations} to use to export
     * {@link PerformanceMonitorBean} instances as JMX MBeans.
     * @param mBeanExportOperations the {@link MBeanExportOperations} to set.
     */
    public void setMBeanExportOperations(MBeanExportOperations mBeanExportOperations) {
        this.mBeanExportOperations = mBeanExportOperations;
    }

    /**
     * Create a new {@link PerformanceMonitorBean} and map it to the provided
     * name.  If the {@link MBeanExportOperations} is set, then the
     * PerformanceMonitorBean will be exported as a JMX MBean.
     * If the PerformanceMonitor already exists, then the existing instance is
     * returned.
     * @param name the value for the {@link PerformanceMonitorBean}
     * @return the created {@link PerformanceMonitorBean}
     * @throws IllegalArgumentException if the MBean value is invalid.
     */
    public synchronized PerformanceMonitor createPerformanceMonitor(
            String name) {
        PerformanceMonitorBean performanceMonitor =
                findPerformanceMonitorBean(name);

        if (performanceMonitor == null) {
            performanceMonitor = new PerformanceMonitorBean();

            if (mBeanExportOperations != null) {
                ObjectName objectName;

                try {
                    objectName = new ObjectName(
                            "org.fishwife.jrugged.spring:type=" +
                                    "PerformanceMonitorBean,name=" + name);
                }
                catch (MalformedObjectNameException e) {
                    throw new IllegalArgumentException(
                            "Invalid MBean Name " + name, e);

                }

                mBeanExportOperations.registerManagedResource(
                        performanceMonitor, objectName);
            }
            addPerformanceMonitorToMap(name, performanceMonitor);
        }

        return performanceMonitor;
    }

    /**
     * Find an existing {@link PerformanceMonitorBean}
     * @param name the value for the {@link PerformanceMonitorBean}
     * @return the found {@link PerformanceMonitorBean}, or null if it is not
     * found.
     */
    public PerformanceMonitorBean findPerformanceMonitorBean(String name) {
        PerformanceMonitor performanceMonitor = findPerformanceMonitor(name);

        if (performanceMonitor instanceof PerformanceMonitorBean) {
            return (PerformanceMonitorBean)performanceMonitor;
        }
        return null;
    }
}
