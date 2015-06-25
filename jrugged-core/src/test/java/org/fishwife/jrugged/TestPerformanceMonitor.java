/* TestPerformanceMonitor.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPerformanceMonitor {

    @Test
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

    @Test
    public void testRunnableWithResultReturnsResultOnSuccess() throws Exception {
        PerformanceMonitor perfMon = new PerformanceMonitor();
        Integer returnResult = 21;

        Integer callResult = perfMon.invoke(new ConstantSuccessPerformer(5), returnResult);

        assertEquals(returnResult, callResult);
    }

    @Test(expected=Exception.class)
    public void testRunnableWithResultReturnsExceptionOnFailure() throws Exception {
        PerformanceMonitor perfMon = new PerformanceMonitor();
        Integer returnResult = 21;

        perfMon.invoke(new OccasionalExceptionPerformer(1), returnResult);
    }

    public class ConstantSuccessPerformer implements Runnable {

        private int _totalNumberOfTimesToLoop;

        public  ConstantSuccessPerformer(int howManyTimesToLoop) {
            _totalNumberOfTimesToLoop = howManyTimesToLoop;
        }

        public void run() {
            for (long i = 0; i < _totalNumberOfTimesToLoop; i++) {

            }
        }
    }

    public class OccasionalExceptionPerformer implements Runnable {

        private int _callsPerException;
        private int _loopCounter;

        public  OccasionalExceptionPerformer(int callsPerException) {
            _callsPerException = callsPerException;
        }

        public void run() {
            _loopCounter++;
            if (_loopCounter % _callsPerException == 0) {
                throw new IllegalStateException("Duh");
            }
        }
    }
}
