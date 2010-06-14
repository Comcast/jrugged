/* Copyright 2009 Comcast Interactive Media, LLC.
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

import java.util.Random;

import org.fishwife.jrugged.aspects.Monitorable;

public class ResponseTweaker {

    @Monitorable("ResponseTweaker")
    public int delay() {
        Random r = new Random();
        int count = r.nextInt(2001);
        try {
            Thread.sleep(count);
        } catch (InterruptedException e) { }
        return count;
    }
    
}
