/* InstrumentationImpact.java
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
package org.fishwife.jrugged.examples.performance;

import org.fishwife.jrugged.PerformanceMonitor;

/** This is an example driver program that measures the amount of
 * overhead introduced by wrapping a method call with a
 * {@link PerformanceMonitor}. */
public class InstrumentationImpact {

    public static void main (String[] args) throws Exception {
        InstrumentationImpact ii = new InstrumentationImpact();
        ii.examplePerformanceImpact();
        ii.exampleExceptionCountsImpact();
        ii.exampleRunningSuccessRPS();
    }

    public void examplePerformanceImpact() throws Exception {
        PerformanceMonitor perfMon = new PerformanceMonitor();
        final FixedDelayPerformer performer = new FixedDelayPerformer(50L);

        long startDelay = System.currentTimeMillis();
        for(int i=0; i<500; i++) {
            perfMon.invoke(performer);
        }
        long endDelay = System.currentTimeMillis();

        long start = System.currentTimeMillis();
        for(int i=0; i<500; i++) {
            performer.run();
        }
        long end = System.currentTimeMillis();

        long deltaDelay = endDelay - startDelay;
        long delta = end - start;
        long difference = deltaDelay - delta;
        long percentEffect = (difference/delta) * 100;

        System.out.println("PerfMon Runtime Total: " + deltaDelay);
        System.out.println("Plain Runtime Total: " + delta);
        System.out.println(String.format("Instrumentation Effect: %s, Difference Percent: %s",
                difference, percentEffect));
    }

    public void exampleExceptionCountsImpact() {
        int numberOfTimesToTryAMethodCall = 500;
        int numberOfAttemptsBeforeThrowingException = 5;
        int expectedNumberOfFailures =
			numberOfTimesToTryAMethodCall /
			numberOfAttemptsBeforeThrowingException;
        int expectedNumberOfSuccess = numberOfTimesToTryAMethodCall -
			expectedNumberOfFailures;

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

        for(int i=0; i<numberOfTimesToTryAMethodCall; i++) {
            try {
                performer.run();
            }
            catch (Exception e) {
                //ignore me.
            }
        }

        System.out.println("Performance Counter Failures: " 
						   + perfMon.getFailureCount()
						   + " Expected Failure Count is: " 
						   + expectedNumberOfFailures);
        System.out.println("Performance Counter Success: " 
						   + perfMon.getSuccessCount()
						   + " Expected Success Count is: " 
						   + expectedNumberOfSuccess);
    }

    public void exampleRunningSuccessRPS() throws Exception {
        PerformanceMonitor perfMon = new PerformanceMonitor();
        final FixedDelayPerformer performer = new FixedDelayPerformer(10L);

        long begin = System.currentTimeMillis();
        int counter = 0;
        // 60 seconds
        while (System.currentTimeMillis() - begin < 60000) {
            counter++;
            perfMon.invoke(performer);
        }

        System.out.println("Counter: " + counter);
        System.out.println("Success rate last minute: " 
						   + perfMon.getSuccessRequestsPerSecondLastMinute());
        System.out.println("Success rate last hour: " 
						   + perfMon.getSuccessRequestsPerSecondLastHour());
        System.out.println("Success rate last day: " 
						   + perfMon.getSuccessRequestsPerSecondLastDay());
        System.out.println("Success rate last lifetime: " 
						   + perfMon.getSuccessRequestsPerSecondLifetime());
    }
}
