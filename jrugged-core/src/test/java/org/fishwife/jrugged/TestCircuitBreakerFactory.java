/* Copyright 2009-2019 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

public class TestCircuitBreakerFactory {

    private CircuitBreakerFactory factory;
    private CircuitBreakerConfig config;

    private static final int TEST_LIMIT = 5;
    private static final long TEST_WINDOW_MILLIS = 30000L;
    private static final long TEST_RESET_MILLIS = 10000L;

    @Before
    public void setUp() {
        factory = new CircuitBreakerFactory();
        config = new CircuitBreakerConfig(TEST_RESET_MILLIS,
                new DefaultFailureInterpreter(TEST_LIMIT, TEST_WINDOW_MILLIS));
    }

    @Test
    public void testCreateCircuitBreaker() {
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreate", config);
        checkBreakerWithDefaultFailureInterpreter(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }

    @Test
    public void testCreateDuplicateCircuitBreaker() {
        String name = "testCreate";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(name, config);
        CircuitBreaker secondBreaker = factory.createCircuitBreaker(name, config);

        assertSame(createdBreaker, secondBreaker);
        checkBreakerWithDefaultFailureInterpreter(createdBreaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }

    @Test
    public void testCreateCircuitBreakerEmptyConfig() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new DefaultFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreateEmpty", emptyConfig);

        checkBreakerWithDefaultFailureInterpreter(breaker, 0, 0, 15000L); // These are the CircuitBreaker Defaults.
    }

    @Test
    public void testCreateCircuitBreakerNullFailureInterpreter() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, null);
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreateEmpty", emptyConfig);

        checkBreakerNoFailureInterpreter(breaker, 15000L); // These are the CircuitBreaker Defaults.
    }

    @Test
    public void testCreateCircuitBreakerDefaultFailureInterpreter() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new DefaultFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker("testDefaultFailureInterpreter", emptyConfig);

        checkBreakerWithDefaultFailureInterpreter(breaker, 0, 0L, 15000L); // These are the CircuitBreaker Defaults.
    }

    @Test
    public void testCreateCircuitBreakerPercentErrPerTimeFailureInterpreter() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new PercentErrPerTimeFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker("testPercentErrFailureInterpreter", emptyConfig);

        checkBreakerWithPercentErrPerTimeFailureInterpreter(breaker,0L, 15000L);
    }

    @Test
    public void testFindANamedCircuitBreaker() {
        String monitorName = "testFind";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(monitorName, config);
        CircuitBreaker foundBreaker = factory.findCircuitBreaker(monitorName);
        assertEquals(createdBreaker, foundBreaker);
    }

    @Test
    public void testFindNonExistentCircuitBreaker() {
        CircuitBreaker foundBreaker = factory.findCircuitBreaker("testNonExistent");
        assertNull(foundBreaker);
    }

    @Test
    public void testGetCircuitBreakerNames() {
        Set<String> testSet = new HashSet<String>();
        testSet.add("one");
        testSet.add("two");
        testSet.add("three");
        testSet.add("four");

        factory.createCircuitBreaker("one", config);
        factory.createCircuitBreaker("two", config);
        factory.createCircuitBreaker("three", config);
        factory.createCircuitBreaker("four", config);
        Set<String> monitorNames = factory.getCircuitBreakerNames();
        assertEquals(testSet, monitorNames);
    }

    @Test
    public void testEmptyPropertyOverrides() {
        Properties overrideProperties = new Properties();
        factory.setProperties(overrideProperties);
        CircuitBreaker breaker = factory.createCircuitBreaker("emptyOverrides", config);
        checkBreakerWithDefaultFailureInterpreter(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }

    @Test
    public void testPropertyOverrides() {
        Properties overrideProperties = new Properties();
        Integer overrideLimit = 10;
        Long overrideResetMillis = 50000L;
        Long overrideWindowMillis = 500000L;
        String name = "testOverrides";

        overrideProperties.put("circuit." + name + ".limit", overrideLimit.toString());
        overrideProperties.put("circuit." + name + ".resetMillis", overrideResetMillis.toString());
        overrideProperties.put("circuit." + name + ".windowMillis", overrideWindowMillis.toString());
        factory.setProperties(overrideProperties);

        CircuitBreaker breaker = factory.createCircuitBreaker(name, config);
        checkBreakerWithDefaultFailureInterpreter(breaker, overrideLimit,  overrideWindowMillis, overrideResetMillis);
    }

    @Test
    public void testInvalidPropertyOverrides() {
        Properties overrideProperties = new Properties();
        String name = "testOverrides";

        overrideProperties.put("circuit." + name + ".limit", "badLimit");
        overrideProperties.put("circuit." + name + ".resetMillis", "badResetMillis");
        overrideProperties.put("circuit." + name + ".windowMillis", "badWindowMillis");
        factory.setProperties(overrideProperties);

        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new DefaultFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker(name, emptyConfig);
        checkBreakerWithDefaultFailureInterpreter(breaker, 0, 0L, 15000L); // These are the CircuitBreaker defaults.
        assertNotNull(breaker);
    }

    private void checkBreakerWithDefaultFailureInterpreter(CircuitBreaker breaker,
                                                           int expectedLimit,
                                                           long expectedWindowMillis,
                                                           long expectedResetMillis) {

        assertNotNull(breaker);

        DefaultFailureInterpreter failureInterpreter = (DefaultFailureInterpreter)breaker.getFailureInterpreter();

        if (failureInterpreter != null) {
            assertEquals(failureInterpreter.getLimit(), expectedLimit);
            assertEquals(failureInterpreter.getWindowMillis(), expectedWindowMillis);
        }

        assertEquals(breaker.getResetMillis(), expectedResetMillis);
    }

    private void checkBreakerWithPercentErrPerTimeFailureInterpreter(CircuitBreaker breaker,
                                                           long expectedWindowMillis,
                                                           long expectedResetMillis) {

        assertNotNull(breaker);

        PercentErrPerTimeFailureInterpreter failureInterpreter = (PercentErrPerTimeFailureInterpreter) breaker.getFailureInterpreter();

        if (failureInterpreter != null) {
            assertEquals(failureInterpreter.getWindowMillis(), expectedWindowMillis);
        }

        assertEquals(breaker.getResetMillis(), expectedResetMillis);
    }

    private void checkBreakerNoFailureInterpreter(CircuitBreaker breaker,
            long expectedResetMillis) {

        assertNotNull(breaker);
        assertEquals(breaker.getResetMillis(), expectedResetMillis);
    }
}
