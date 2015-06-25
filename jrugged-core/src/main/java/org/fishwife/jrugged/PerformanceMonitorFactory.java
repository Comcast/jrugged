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
package org.fishwife.jrugged;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to create new {@link PerformanceMonitor} instances and keep track of
 * them.
 */
public class PerformanceMonitorFactory {

    private final Map<String, PerformanceMonitor> performanceMonitorMap =
            new ConcurrentHashMap<String, PerformanceMonitor>();

    /**
     * Create an empty PerformanceMonitorFactory.
     */
    public PerformanceMonitorFactory() {
    }

    /**
     * Create a new {@link PerformanceMonitor} and map it to the provided name.
     * If the PerformanceMonitor already exists, then the existing instance is
     * returned.
     * @param name the name for the {@link PerformanceMonitor}
     * @return the created {@link PerformanceMonitor}
     */
    public synchronized PerformanceMonitor createPerformanceMonitor(String name) {
        PerformanceMonitor performanceMonitor = findPerformanceMonitor(name);

        if (performanceMonitor == null) {
            performanceMonitor = new PerformanceMonitor();
            addPerformanceMonitorToMap(name, performanceMonitor);
        }
        return performanceMonitor;
    }

    /**
     * Find an existing {@link PerformanceMonitor}
     * @param name the name for the {@link PerformanceMonitor}
     * @return the found {@link PerformanceMonitor}, or null if it is not found.
     */
    public PerformanceMonitor findPerformanceMonitor(String name) {
        return performanceMonitorMap.get(name);
    }

    /**
     * Get the {@link Set} of created performance monitor names.
     * @return the {@link Set} of names.
     */
    public Set<String> getPerformanceMonitorNames() {
        return performanceMonitorMap.keySet();
    }

    /**
     * Add a {@link PerformanceMonitor} to the map.
     * @param name the name for the {@link PerformanceMonitor}
     * @param performanceMonitor the {@link PerformanceMonitor} to add.
     */
    protected void addPerformanceMonitorToMap(String name, PerformanceMonitor performanceMonitor) {
        performanceMonitorMap.put(name, performanceMonitor);
    }
}
