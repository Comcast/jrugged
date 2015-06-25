/* PercentileTracker.java
 *
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * This class implements a way to get percentiles from a list of samples within
 * a given time range using the algorithm described at
 * <a href="http://cnx.org/content/m10805/latest/">http://cnx.org/content/m10805/latest/</a>.
 * The percentile does not sample itself; it merely computes the asked for percentile
 * when called.
 */
public class PercentileTracker {
    private long windowMillis;

    protected ConcurrentSkipListMap<Long, Double> cslm = new ConcurrentSkipListMap<Long, Double>();

    public PercentileTracker(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    /** Updates the average with the latest measurement.
     *  @param sample the latest measurement in the rolling average */
    public synchronized void update(double sample) {
        long now = System.currentTimeMillis();

        removeOutOfTimeWindowEntries();
        cslm.put(now, sample);
    }

    /**
     * Returns a computed percentile value.
     *
     * @param requestedPercentile Which whole number percentile value needs to be calculated and returned
     * @return double the percentile
     */
    public double getPercentile(int requestedPercentile) {
        return getPercentile(requestedPercentile, new ArrayList<Double>(cslm.values()));
    }

    protected double getPercentile(int requestedPercentile, ArrayList<Double> values) {
        if (values == null || values.size() == 0) {
            return 0d;
        }

        Collections.sort(values);
        Double[] mySampleSet = values.toArray(new Double[values.size()]);

        //This is the Excel Percentile Rank
        double rank = (((double) requestedPercentile / 100d) * (values.size() - 1)) + 1;

        //This is the Weighted Percentile Rank
        //double rank = ((double) requestedPercentile / 100d) * (values.size() + 1);

        double returnPercentile;

        int integerRank = (int) rank;
        double remainder = rank - integerRank;

        if (remainder > 0) {
            //Interpolate the percentile
            double valueAtRankIr = mySampleSet[integerRank - 1];
            double valueAtRankIrPlusOne = mySampleSet[integerRank];
            returnPercentile = remainder * (valueAtRankIrPlusOne - valueAtRankIr) + valueAtRankIr;
        }
        else {
            // Use the rank to find the 'exact' percentile.
            returnPercentile = mySampleSet[integerRank - 1];
        }
        return returnPercentile;

    }

    private void removeOutOfTimeWindowEntries() {
        if (cslm.isEmpty()) {
            return;
        }

        // Optimization - if the last entry is also outside
        // the time window, all items in the list can be cleared.
        if (System.currentTimeMillis() - cslm.lastKey() > windowMillis) {
            cslm.clear();
            return;
        }

        while ((cslm.lastKey() - cslm.firstKey()) > windowMillis) {
            cslm.pollFirstEntry(); //The first entry is now too old, remove it
        }
    }
}
