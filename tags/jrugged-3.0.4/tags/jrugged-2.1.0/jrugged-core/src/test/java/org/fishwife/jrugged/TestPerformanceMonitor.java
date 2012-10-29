/* TestPerformanceMonitor.java
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

import junit.framework.TestCase;

public class TestPerformanceMonitor extends TestCase {

    public void testSuccessAndFailureCounts() {
        int numberOfTimesToTryAMethodCall = 500;
        int numberOfAttemptsBeforeThrowingException = 5;
        int expectedNumberOfFailures =
			numberOfTimesToTryAMethodCall
			/ numberOfAttemptsBeforeThrowingException;
        int expectedNumberOfSuccess = numberOfTimesToTryAMethodCall
			- expectedNumberOfFailures;

        PerformanceMonitor perfMon = new PerformanceMonitor();
        final OccasionalExceptionPerformer performer =
			new OccasionalExceptionPerformer(numberOfAttemptsBeforeThrowingException);

        for(int i=0; i<numberOfTimesToTryAMethodCall; i++) {
            try {
                perfMon.invoke(performer);
            } catch (Exception e) {
                //ignore me.
            }
        }

        for(int i=0;i<numberOfTimesToTryAMethodCall;i++) {
            try {
                performer.run();
            } catch (Exception e) {
                //ignore me.
            }
        }

        assertEquals(expectedNumberOfFailures, perfMon.getFailureCount());
        assertEquals(expectedNumberOfSuccess, perfMon.getSuccessCount());
    }

    public class OccasionalExceptionPerformer implements Runnable {

        private int _callsPerException;
        private int _loopCounter;

        public  OccasionalExceptionPerformer(int callsPerException) {
            _callsPerException = callsPerException;
        }

        public void run() {
            _loopCounter++;
            if (_loopCounter % _callsPerException == 0)
            {
                throw new IllegalStateException("Duh");
            }
        }
    }
}
