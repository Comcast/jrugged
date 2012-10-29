/* FlowRateExceededException.java
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
package org.fishwife.jrugged;

public class FlowRateExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    private double rate = 0;
    private double maximumRate = 0;

    public FlowRateExceededException() {
    }

    public FlowRateExceededException(String message, double rate, double maximumRate) {
        super(message);
        this.rate = rate;
        this.maximumRate = maximumRate;
    }

    public FlowRateExceededException(double rate, double maximumRate) {
        this("Rate Exceeded (Max=" + maximumRate + ", Current=" + rate + ")", rate, maximumRate);
    }
}
