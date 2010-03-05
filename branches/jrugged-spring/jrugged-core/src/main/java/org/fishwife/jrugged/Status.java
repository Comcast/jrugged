/* Status.java
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

/** Various components in the system need to report their status. This
 *  <code>enum</code> standardizes on a set of conventions for reporting
 *  this. Each {@link Status} has an integer <code>value</code> which can
 *  be compared; higher values represent higher levels of functionality.
 *  In addition, each {@link Status} has a {@link String} <code>signal</code>
 *  which is either GREEN, YELLOW, or RED.
 *  <p>
 *  The general notion is that the whole system (or a subsystem) should
 *  only report GREEN status (UP) if everything is working as designed.
 *  When subsystems start to go down, or the current system stops working,
 *  status should drop to either YELLOW (DEGRADED) or RED (DOWN), depending
 *  on whether service was still available in some form. For example, if
 *  a cache subsystem goes down, we might report a YELLOW status because
 *  we can attempt to serve without a cache. However, if a required database
 *  goes down, we probably need to report a RED status, unable to serve
 *  requests.
 *  <p>
 *  A "rugged" system should be able to accurately (and responsively)
 *  report on its status even if it is unable to perform its main functions.
 *  This will assist operators in diagnosing the problem; a hung process
 *  tells no tales.
 */
public enum Status {
    /** Unrecoverable: we're basically dead for good. */
    FAILED(-2,"RED"),		

    /** Being initialized: not yet ready to serve. */
    INIT(-1,"RED"),

    /** Currently unavailable. */
    DOWN(0,"RED"),

    /** Something's wrong, but we can still serve requests. */
    DEGRADED(1,"YELLOW"),

    /** Everything is operating as expected. */
    UP(2,"GREEN");

    private final int value;
    private final String signal;

    Status(int value, String signal) {
		this.value = value;
		this.signal = signal;
    }

    /** Returns the current status as an integer. Higher values are for 
     *  states of higher functionality/availability. */
    public int getValue() { return value; }

    /** Returns the current GREEN/YELLOW/RED dashboard indicator
     *  corresponding to this status. */
    public String getSignal() { return signal; }

    /** Returns whether the status indicates a system that is able
     *  to serve requests. */
    public boolean isAvailable() { return (value > 0); }
};