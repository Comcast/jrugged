/* RequestCounter.java
 * 
 * Copyright (C) 2009 Jonathan T. Moore
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

import java.util.concurrent.Callable;

/** This is a statistics wrapper that counts total requests, as well
 *  as how many succeed and how many fail. This class can be polled
 *  periodically to measure request rates, success rates, and failure
 *  rates.
 */
public class RequestCounter implements ServiceWrapper {
    private long numRequests = 0L;
    private long numSuccesses = 0L;
    private long numFailures = 0L;

    private synchronized void succeed() {
	numRequests++;
	numSuccesses++;
    }

    private synchronized void fail() {
	numRequests++;
	numFailures++;
    }

    public <T> T invoke(Callable<T> c) throws Exception {
	try {
	    T result = c.call();
	    succeed();
	    return result;
	} catch (Exception e) {
	    fail();
	    throw e;
	}
    }

    public void invoke(Runnable r) throws Exception {
	try {
	    r.run();
	    succeed();
	} catch (Exception e) {
	    fail();
	    throw e;
	}
    }

    public <T> T invoke(Runnable r, T result) throws Exception {
	try {
	    r.run();
	    succeed();
	    return result;
	} catch (Exception e) {
	    fail();
	    throw e;
	}
    }

    /** Samples the current counts.
     *  @return an array of three <code>longs</code>: the total
     *    number of requests, the number of successful requests,
     *    and the number of failed requests.
     */
    public synchronized long[] sample() {
	long[] out = { numRequests, numSuccesses, numFailures };
	return out;
    }

}