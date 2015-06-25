/* TestPercentErrPerTimeFailureInterpreter.java
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPercentErrPerTimeFailureInterpreter {

    private class DummyRunnable implements Runnable {

        public void run() {

        }
    }

    private class DummyRunnableException implements Runnable {

        public void run() {
            throw new RuntimeException();
        }
    }

    private class DummyMyRunnableException implements Runnable {

        public void run() {
            throw new MyRuntimeException();
        }
    }

    private class MyRuntimeException extends RuntimeException {

    }

    @Test
    public void testTripsWhenNoWindowConditionsExist() {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();

        assertTrue(pept.shouldTrip(new Exception()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoesNotTripWhenExceptionIsIgnored() {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        pept.setIgnore(new Class[] {MyRuntimeException.class});

        assertFalse(pept.shouldTrip(new MyRuntimeException()));
        assertTrue(pept.shouldTrip(new Exception()));
    }

    @Test
    public void testTripsWhenPercentErrorInWindowIsGreaterThanConfigured() throws Exception {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        RequestCounter rc = new RequestCounter();
        pept.setRequestCounter(rc);
        pept.setPercent(51);
        pept.setWindowMillis(5000);

        try {
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
        }
        catch (Exception e) {
            pept.shouldTrip(e);
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertTrue(pept.shouldTrip(e));
        }
    }

    @Test
    public void testTripsWhenPercentErrorInWindowIsEqualConfigured() throws Exception {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        RequestCounter rc = new RequestCounter();
        pept.setRequestCounter(rc);
        pept.setPercent(50);
        pept.setWindowMillis(5000);

        try {
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
        }
        catch (Exception e) {
            pept.shouldTrip(e);
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertTrue(pept.shouldTrip(e));
        }
    }

    @Test
    public void testDoesNotTripWhenPercentErrorInWindowIsLessThanConfigured() throws Exception {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        RequestCounter rc = new RequestCounter();
        pept.setRequestCounter(rc);
        pept.setPercent(51);
        pept.setWindowMillis(2500);

        try {
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
        }
        catch (Exception e) {
            pept.shouldTrip(e);
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }
    }

    @Test
    public void testDoesNotTripWhenRequestCountInWindowIsLessThanThreshold() throws Exception {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        RequestCounter rc = new RequestCounter();
        pept.setRequestCounter(rc);
        pept.setPercent(51);
        pept.setWindowMillis(250000);
        pept.setRequestThreshold(8);

        try {
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
            rc.invoke(new DummyRunnable());
        } catch (Exception e) {
            pept.shouldTrip(e);
        }

        try {
            rc.invoke(new DummyRunnableException());
        } catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        } catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        } catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        //seventh total request, fourth failure, pushes percentage to 57%, but should not trip because total number
        //of requests is below threshold of 8
        try {
            rc.invoke(new DummyRunnableException());
        } catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }
    }

    @Test
    public void testTripsOnSecondWindowWithRequestThresholdConfigured() throws Exception {
        PercentErrPerTimeFailureInterpreter pept = new PercentErrPerTimeFailureInterpreter();
        RequestCounter rc = new RequestCounter();
        pept.setRequestCounter(rc);
        pept.setPercent(50);
        pept.setWindowMillis(1000);
        pept.setRequestThreshold(5);

        //
        // First window - 1 valid request, 3 failures valid requests, less than request window, not tripped
        //
        rc.invoke(new DummyRunnable());

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        // Sleep to reset the window
        Thread.sleep(1100);

        //
        // Second Window - 2 failures, 2 success, 1 failure - should trip at the end.
        //
        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertFalse(pept.shouldTrip(e));
        }

        rc.invoke(new DummyRunnable());
        rc.invoke(new DummyRunnable());

        try {
            rc.invoke(new DummyRunnableException());
        }
        catch (Exception e) {
            assertTrue(pept.shouldTrip(e));
        }


    }

}

