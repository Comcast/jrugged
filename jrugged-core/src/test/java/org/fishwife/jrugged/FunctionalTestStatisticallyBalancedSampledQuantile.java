/* TestPerformanceMonitor.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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
import sun.misc.Perf;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class FunctionalTestStatisticallyBalancedSampledQuantile {

    @Test
    public void testRunnableWithResultReturnsResultOnSuccess() throws Exception {
        FunctionalStatisticallyBalancedPerformanceMonitor perfMon = new FunctionalStatisticallyBalancedPerformanceMonitor();

        Integer returnResult = 5000;

        for (int i = 0; i < 300; i++){
            perfMon.invoke(new ConstantSuccessPerformer(1));
        }

        System.out.println(perfMon.get95thPercentileSuccessLatencyLastMinute());

        System.out.println(perfMon.get95thPercentileSuccessLatencyLastHour());

        System.out.println(perfMon.get95thPercentileSuccessLatencyLastDay());

        // assertEquals(returnResult, callResult);
    }

    public class ConstantSuccessPerformer implements Runnable {

        private int _totalNumberOfTimesToLoop;

        public  ConstantSuccessPerformer(int howManyTimesToLoop) {
            _totalNumberOfTimesToLoop = howManyTimesToLoop;
        }

        public void run() {
            for (long i = 0; i < _totalNumberOfTimesToLoop; i++) {
                try {
                    URL url = new URL("http://www.yahoo.com");
                    try {
                        HttpURLConnection con =    (HttpURLConnection) url.openConnection();

                        con.setRequestMethod("GET");

                        //add request header
                        con.setRequestProperty("User-Agent", "Chrome 41.0");

                        int responseCode = con.getResponseCode();
                    }
                    catch (IOException e){
                        System.out.println("bad io");
                    }
                }
                catch (MalformedURLException m){
                    System.out.println("Bad url");
                }
            }
        }
    }

}