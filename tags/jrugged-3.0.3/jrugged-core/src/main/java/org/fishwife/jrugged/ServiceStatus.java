/* ServiceStatus.java
 *
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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
import java.util.List;

/**
 * A ServiceStatus is used by a {@link MonitoredService} to report it's status.
 * The status includes the name of the service, the {@link Status} for the
 * service, and the {@link List} of reasons for the status.
 */
public class ServiceStatus {
    private final String name;
    private final Status status;
    private final List<String> reasons;

    /**
     * Constructor with name and {@link Status}.
     * @param name the name of the service.
     * @param status the {@link Status} of the service.
     */
    public ServiceStatus(String name, Status status) {
        this.name = name;
        this.status = status;
        this.reasons = new ArrayList<String>();
    }

    /**
     * Constructor with name, {@link Status}, and reason.
     * @param name the name of the service.
     * @param status the {@link Status} of the service.
     * @param reason the reason for the status.
     */
    public ServiceStatus(String name, Status status, String reason) {
        this.name = name;
        this.status = status;
        this.reasons = new ArrayList<String>();
        if (reason != null) {
            reasons.add(reason);
        }
    }

    /**
     * Constructor with name, {@link Status}, and a {@link List} of reasons.
     * @param name the name of the service.
     * @param status the {@link Status} of the service.
     * @param reasons the {@link List} of reasons for the status.
     */
    public ServiceStatus(String name, Status status, List<String> reasons) {
        this.name = name;
        this.status = status;
        this.reasons = new ArrayList<String>();
        if (reasons != null) {
            this.reasons.addAll(reasons);
        }
    }

    /**
     * Get the name of the service.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link Status} of the service.
     * @return the {@link Status}.
     */
    public Status getStatus () {
        return status;
    }

    /**
     * Get the {@link List} of reasons for the status.
     * @return the {@link List} of reasons.
     */
    public List<String> getReasons() {
        return new ArrayList<String>(reasons);
    }
}
