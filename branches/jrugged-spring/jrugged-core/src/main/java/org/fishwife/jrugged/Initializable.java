/* Initializable.java
 * 
 * Copyright 2009-2010 Comcast Interactive Media, LLC.
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

/** Clients who use the {@link Initializer} should implement this interface;
 *  this lets them do, for example:
 *  <pre>
 *    new Initializer(this).initialize();
 *  </pre>
 *  and the Initializer will take care of retrying an initialization.
 *
 *  This pattern is good for subsystems that need to talk to something
 *  that is currently down at startup time. For example, if your database
 *  is down when your app server starts up, this can let your app server
 *  keep trying to connect without getting wedged or needing to restart.
 */
public interface Initializable {

    /** Makes an initialization attempt. If an exception is thrown, assume
     *  attempt failed.
     */
    public void tryInit() throws Exception;

    /** Called by the initializer after background initialization succeeds.
     *  Can be used to mark the client as "ready to serve" or "active", 
     *  etc.
     */
    public void afterInit();
}
