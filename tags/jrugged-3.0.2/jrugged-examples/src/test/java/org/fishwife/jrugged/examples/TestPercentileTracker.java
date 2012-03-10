/* TestPercentileTracker.java
 * 
 * Copyright 2009-2011 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.examples;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static java.lang.Thread.sleep;
import static org.junit.Assert.fail;

public class TestPercentileTracker {

    ArrayList<Double> normalizedTestArray;

    @Before
    public void setup() {
        normalizedTestArray = new ArrayList<Double>() {
            {add(0d);
             add(1d);
             add(2d);
             add(3d);
             add(4d);
             add(5d);
             add(6d);
             add(7d);
             add(8d);
             add(9d);
             add(10d);
            }
        };
    }

    @Test
    public void test90ithPercentile () {
        PercentileTracker pt = new PercentileTracker(5000);
        double value = pt.getPercentile(90, normalizedTestArray);
        Assert.assertEquals(9d, value, 0);
    }

    @Test
    public void test95ithPercentile () {
        PercentileTracker pt = new PercentileTracker(5000);
        double value = pt.getPercentile(95, normalizedTestArray);
        Assert.assertEquals(9.5d, value, 0);
    }

    @Test
    public void testUpdate() {
        PercentileTracker pt = new PercentileTracker(5000);
        for (int i = 0; i < 11; i++) {
            pt.update((double)i);
            try {
                sleep(10);
            } catch (InterruptedException e) {
                fail();
            }
        }
        double value = pt.getPercentile(95);
        Assert.assertEquals(9.5d, value, 0);

    }

    @Test
    public void testEmptyPercentileTrackerDoesNotNPE() {
        PercentileTracker pt = new PercentileTracker(5000);

        try {
            pt.getPercentile(99);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testPTWholeListOutsideWindow() {
        PercentileTracker pt = new PercentileTracker(200);
        for (int i = 0; i < 11; i++) {
            pt.update((double)i);
            try {
                sleep(10);
            } catch (InterruptedException e) {
                fail();
            }
        }
        Assert.assertEquals(pt.cslm.entrySet().size(), 11);

        try {
            sleep(300);
        } catch (InterruptedException e) {
            fail();
        }

        pt.update(1d);
    }
}
