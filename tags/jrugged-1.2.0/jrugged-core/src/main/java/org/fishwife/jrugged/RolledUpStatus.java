/* RolledUpStatus.java
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
package org.fishwife.jrugged;

import java.util.Collection;

/** A {@link RolledUpStatus} provides for grouping subsystems together in
 *  a single system for status reporting purposes. Subsystems are divided
 *  into <em>critical</em> and <em>noncritical</em> groups. The system as
 *  a whole will only report UP status (GREEN) if all subsystems, critical
 *  and noncritical alike, are likewise UP. Critical systems are necessary
 *  for providing functionality within the system, so the system as a whole
 *  reports the lowest (least functional) status of any critical systems
 *  which are not UP. If all critical systems are UP, but one or more
 *  noncritical systems are not UP, then the system as a whole will be in
 *  the DEGRADED (YELLOW) state.
 */
public class RolledUpStatus implements Monitorable {
    private Collection<? extends Monitorable> criticals;
    private Collection<? extends Monitorable> noncriticals;

    /** Initializes the {@link RolledUpStatus} with its component
     *  subsystems.
     *  @param criticals the set of {@link Monitorable} subsystems
     *     that are necessary for the system as a whole to be functional
     *  @param noncriticals the set of <code>Monitorable</code> subsystems
     *     that are part of the system as a whole but which are not
     *     strictly necessary for functionality.
     */
    public RolledUpStatus(Collection<? extends Monitorable> criticals,
			  Collection<? extends Monitorable> noncriticals) {
	this.criticals = criticals;
	this.noncriticals = noncriticals;
    }

    /** Returns the current {@link Status} of the system as a whole. */
    public Status getStatus() {
	Status result = Status.UP;
	for(Monitorable m : noncriticals) {
	    if (!Status.UP.equals(m.getStatus())) {
		result = Status.DEGRADED;
	    }
	}
	for(Monitorable m : criticals) {
	    Status subStatus = m.getStatus();
	    if (subStatus.getValue() < result.getValue()) {
		result = subStatus;
	    }
	}
	return result;
    }
}