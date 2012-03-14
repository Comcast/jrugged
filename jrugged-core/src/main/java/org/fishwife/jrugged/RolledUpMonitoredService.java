/* Copyright 2009-2012 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link RolledUpMonitoredService} provides for grouping
 *  {@link MonitoredService} instances together into a single system for status
 *  reporting purposes. Subsystems are divided into <em>critical</em> and
 *  <em>noncritical</em> groups. The system as a whole will only report UP
 *  status (GREEN) if all subsystems, critical and noncritical alike, are
 *  likewise UP. Critical systems are necessary for providing functionality
 *  within the system, so the system as a whole reports the lowest
 *  (least functional) status of any critical systems which are not UP. If all
 *  critical systems are UP, but one or more noncritical systems are not UP,
 *  then the system as a whole will be in the DEGRADED (YELLOW) state.
 */
public class RolledUpMonitoredService implements MonitoredService {
    private String name;
    private Collection<? extends MonitoredService> criticals;
    private Collection<? extends MonitoredService> noncriticals;

    /** Initializes the {@link RolledUpMonitoredService} with its component
     *  subsystems.
     *  @param name the name for this rolled up service.
     *  @param criticals the set of {@link MonitoredService} subsystems
     *     that are necessary for the system as a whole to be functional
     *  @param noncriticals the set of <code>MonitoredService</code> subsystems
     *     that are part of the system as a whole but which are not
     *     strictly necessary for functionality.
     */
    public RolledUpMonitoredService(String name,
            Collection<? extends MonitoredService> criticals,
            Collection<? extends MonitoredService> noncriticals) {
        this.name = name;
	    this.criticals = criticals;
	    this.noncriticals = noncriticals;
    }

    /**
     * Get the {@link ServiceStatus} for the rolled up service.  The
     * name for the ServiceStatus is the name of this rolled up service.
     * The {@link Status} is computed based on the status of the critical
     * and non-critical subsystems.  The {@link List} of reasons contains the
     * reasons reported for each system.
     * @return the {@link ServiceStatus}.
     */
    public ServiceStatus getServiceStatus() {
        List<String> reasons = new ArrayList<String>();

        Status criticalStatus = Status.UP;

        for (MonitoredService m : criticals) {
            ServiceStatus serviceStatus = m.getServiceStatus();
            Status status = serviceStatus.getStatus();

            if (statusIsNotUp(status)) {
                reasons.addAll(serviceStatus.getReasons());
            }

            if (status.getValue() < criticalStatus.getValue()) {
                criticalStatus = status;
            }
        }

        Status result = Status.UP;

        for (MonitoredService m : noncriticals) {
            ServiceStatus serviceStatus = m.getServiceStatus();
            Status status = serviceStatus.getStatus();

            if (statusIsNotUp(status)) {
                reasons.addAll(serviceStatus.getReasons());
                result = Status.DEGRADED;
            }
        }

        if (criticalStatus.getValue() < result.getValue()) {
            result = criticalStatus;
        }

        return new ServiceStatus(name, result, reasons);
    }

    /**
     * Get the {@link List} of {@link ServiceStatus} instances for all
     * subsystems.
     * @return the {@link List} of {@link ServiceStatus} instances.
     */
    public List<ServiceStatus> getAllStatuses() {
        List<ServiceStatus> list = getCriticalStatuses();
        list.addAll(getNonCriticalStatuses());
        return list;
    }

    /**
     * Get the {@link List} of {@link ServiceStatus} instances for all
     * critical subsystems.
     * @return the {@link List} of {@link ServiceStatus} instances.
     */
    public List<ServiceStatus> getCriticalStatuses() {
        List<ServiceStatus> list = new ArrayList<ServiceStatus>();

        for (MonitoredService m : criticals) {
            ServiceStatus serviceStatus = m.getServiceStatus();
            list.add(serviceStatus);
        }
        return list;
    }

    /**
     * Get the {@link List} of {@link ServiceStatus} instances for all
     * non-critical subsystems.
     * @return the {@link List} of {@link ServiceStatus} instances.
     */
    public List<ServiceStatus> getNonCriticalStatuses() {
        List<ServiceStatus> list = new ArrayList<ServiceStatus>();

        for (MonitoredService m : noncriticals) {
            ServiceStatus serviceStatus = m.getServiceStatus();
            list.add(serviceStatus);
        }
        return list;
    }

    private boolean statusIsNotUp(Status status) {
        return Status.UP != status;
    }
}
