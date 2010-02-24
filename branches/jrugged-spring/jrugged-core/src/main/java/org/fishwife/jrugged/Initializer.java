/* Initializer.java
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

/** An {@link Initializer} allows a client to retry failed service 
 *  initializations in the background. For example, the initial 
 *  connection to a remote service may fail; the Initializer will take
 *  responsibility for continuing to retry that connection in a
 *  background thread (so that other services can try to initialize
 *  in the meantime). When initialization succeeds, the background
 *  thread terminates and the client service can enter normal operation.
 *  <p>
 *  Sample usage:
 *  <pre>
    public class Service implements Initializable, Monitorable {
        private Status status = Status.INIT;
        public Service() {
            new Initializer(this).initialize();
        }
        public void tryInit() throws Exception {
            // attempt an initialization here ...
        }
        public void afterInit() { status = Status.UP; }
        public Status getStatus() { return status; }
    }
 *  </pre>
 */
public class Initializer implements Runnable {

    /** By default, keep trying to initialize forever. */
    private int maxRetries = Integer.MAX_VALUE;

    /** Number of initialization attempts we have made. */
    private int numAttempts = 0;

    /** Retry an initialization every 60 seconds by default. */
    private long retryMillis = 60 * 1000L;

    /** This is the guy we're trying to initialize. */
    private Initializable client;

    /** Current status. */
    private boolean initialized = false;

    /** Background initializer thread. */
    private Thread thread;

    /** Set this to true and interrupt thread to cleanly shutdown. */
    private boolean cancelled = false;

    public Initializer(Initializable client) { this.client = client; }

    /** Sets up the initialization retry process. */
    public void initialize() {
	thread = new Thread(this);
	thread.start();
    }

    /** Shuts down the background retry process. If you are using the
     *  Spring framework, for example, if the client implements
     *  DisposableBean you can have the destroy() method of the client
     *  call this method to cleanly shutdown. */
    public void destroy() {
	cancelled = true;
	if (thread != null) thread.interrupt();
    }

    public void run() {
	while(!initialized && numAttempts < maxRetries && !cancelled) {
	    try {
		numAttempts++;
		client.tryInit();
		initialized = true;
		client.afterInit();
	    } catch (Exception e) {
		try {
		    Thread.sleep(retryMillis);
		} catch (InterruptedException ie) {
		    // nop
		}
	    }
	}
    }

    public boolean isInitialized() { return initialized; }
    public boolean isCancelled() { return cancelled; }
    public int getNumAttempts() { return numAttempts; }
    public void setMaxRetries(int n) { maxRetries = n; }
    public void setRetryMillis(long m) { retryMillis = m; }
}