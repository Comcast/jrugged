/* TestCircuitBreakerBean.java
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
package org.fishwife.jrugged.spring;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.fishwife.jrugged.BreakerException;
import org.junit.Before;
import org.junit.Test;

public class TestCircuitBreakerBean {

    private CircuitBreakerBean impl;
    private final Object out = new Object();
    private Callable<Object> call;

    @Before
    public void setUp() {
        impl = new CircuitBreakerBean();
        call = new Callable<Object>() {
            public Object call() throws Exception {
                return out;
            }
        };
    }

    @Test
    public void startsEnabled() throws Exception {
        assertSame(out, impl.invoke(call));
    }

    @Test
    public void isenabledIfConfiguredAsNotDisabled() throws Exception {
        impl.setDisabled(false);
        impl.afterPropertiesSet();
        assertSame(out, impl.invoke(call));
    }

    @Test
    public void canBeDisabled() throws Exception {
        impl.setDisabled(true);
        impl.afterPropertiesSet();
        try {
            impl.invoke(call);
            fail("Should have thrown CircuitBreakerException");
        } catch (BreakerException cbe) {
        }
    }
}
