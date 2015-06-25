/* TestLatencyTracker.java
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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestLatencyTracker {
    private LatencyTracker impl;

    @Before
    public void setUp() {
        impl = new LatencyTracker();
    }

    @Test
    public void testCallableSuccess() throws Exception {
        final Object o = new Object();

        Object result = impl.invoke(new Callable<Object>() {
            public Object call() throws Exception {
                Thread.sleep(1);
                return o;
            }
            });

        assertSame(result, o);
        assertTrue(impl.getLastSuccessMillis() > 0);
        assertEquals(0, impl.getLastFailureMillis());
    }

    @Test
    public void testCallableFailure() throws Exception {

        try {
            impl.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                Thread.sleep(1);
                throw new Exception();
                }
            });
            fail("should have thrown exception");
        }
        catch (Exception expected) {
        }

        assertTrue(impl.getLastFailureMillis() > 0);
        assertEquals(0, impl.getLastSuccessMillis());
    }
}
