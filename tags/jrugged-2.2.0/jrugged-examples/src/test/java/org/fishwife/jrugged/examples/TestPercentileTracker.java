package org.fishwife.jrugged.examples;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
}
