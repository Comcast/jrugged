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
