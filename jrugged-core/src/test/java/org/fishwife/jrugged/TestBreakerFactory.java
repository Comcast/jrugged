/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

public class TestBreakerFactory {

    private BreakerFactory factory;
    private CircuitBreakerConfig circuitBreakerConfig;
    private SkepticBreakerConfig skepticBreakerConfig;
	
    private static final int TEST_LIMIT = 5;
    private static final long TEST_WINDOW_MILLIS = 30000L;
    private static final long TEST_RESET_MILLIS = 10000L;
    
    private static final long TEST_WAIT_BASE = 2000L;
    private static final long TEST_WAIT_MULT = 200L;
    private static final long TEST_GOOD_BASE = 300000L;
    private static final long TEST_GOOD_MULT = 50L;
    private static final long TEST_MAX_LEVEL = 20L;

    @Before
    public void setUp() {
        factory = new BreakerFactory();
        circuitBreakerConfig = new CircuitBreakerConfig(TEST_RESET_MILLIS,
                new DefaultFailureInterpreter(TEST_LIMIT, TEST_WINDOW_MILLIS));
        skepticBreakerConfig = new SkepticBreakerConfig(TEST_GOOD_BASE,
        		TEST_GOOD_MULT, TEST_WAIT_BASE, TEST_WAIT_MULT, TEST_MAX_LEVEL,
        		new DefaultFailureInterpreter(TEST_LIMIT, TEST_WINDOW_MILLIS));
    }

    @Test
    public void testCreateCircuitBreaker() {
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreate", circuitBreakerConfig);
        checkCircuitBreaker(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }
    
    @Test
    public void testCreateSkepticBreaker() {
        SkepticBreaker breaker = factory.createSkepticBreaker("testCreate", skepticBreakerConfig);
        checkSkepticBreaker(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_GOOD_BASE,
        		TEST_GOOD_MULT, TEST_WAIT_BASE, TEST_WAIT_MULT, TEST_MAX_LEVEL);
    }

	@Test
    public void testCreateDuplicateCircuitBreaker() {
        String name = "testCreate";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(name, circuitBreakerConfig);
        CircuitBreaker secondBreaker = factory.createCircuitBreaker(name, circuitBreakerConfig);

        assertSame(createdBreaker, secondBreaker);
        checkCircuitBreaker(createdBreaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }
	
	@Test
    public void testCreateDuplicateSkepticBreaker() {
        String name = "testCreate";
        SkepticBreaker createdBreaker = factory.createSkepticBreaker(name, skepticBreakerConfig);
        SkepticBreaker secondBreaker = factory.createSkepticBreaker(name, skepticBreakerConfig);

        assertSame(createdBreaker, secondBreaker);
        checkSkepticBreaker(createdBreaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_GOOD_BASE,
        		TEST_GOOD_MULT, TEST_WAIT_BASE, TEST_WAIT_MULT, TEST_MAX_LEVEL);
    }

    @Test
    public void testCreateCircuitBreakerEmptyConfig() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new DefaultFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreateEmpty", emptyConfig);

        checkCircuitBreaker(breaker, 0, 0, 15000L); // These are the CircuitBreaker Defaults.
    }
    
    @Test
    public void testCreateSkepticBreakerEmptyConfig() {
        SkepticBreakerConfig emptyConfig =
                new SkepticBreakerConfig(-1, -1, -1, -1, -1,
                		new DefaultFailureInterpreter());
        SkepticBreaker breaker = factory.createSkepticBreaker("testCreateEmpty", emptyConfig);
        
        checkSkepticBreaker(breaker, 0, 0, 600000L,
        		100L, 1000L, 100L, 20L); // These are the SkepticBreaker Defaults.
    }

    @Test
    public void testCreateCircuitBreakerNullFailureInterpreter() {
        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, null);
        CircuitBreaker breaker = factory.createCircuitBreaker("testCreateEmpty", emptyConfig);

        checkCircuitBreakerNoFailureInterpreter(breaker, 15000L); // These are the CircuitBreaker Defaults.
    }
    
    @Test
    public void testCreateSkepticBreakerNullFailureInterpreter() {
        SkepticBreakerConfig emptyConfig =
                new SkepticBreakerConfig(-1, -1, -1, -1, -1, null);
        SkepticBreaker breaker = factory.createSkepticBreaker("testCreateEmpty", emptyConfig);
        
        checkSkepticBreakerNoFailureInterpreter(breaker, 600000L,
        		100L, 1000L, 100L, 20L); // These are the SkepticBreaker Defaults.
    }

	@Test
    public void testFindANamedCircuitBreaker() {
        String monitorName = "testFind";
        CircuitBreaker createdBreaker = factory.createCircuitBreaker(monitorName, circuitBreakerConfig);
        CircuitBreaker foundBreaker = factory.findCircuitBreaker(monitorName);
        assertEquals(createdBreaker, foundBreaker);
    }
	
	@Test
    public void testFindANamedSkepticBreaker() {
        String monitorName = "testFind";
        SkepticBreaker createdBreaker = factory.createSkepticBreaker(monitorName, skepticBreakerConfig);
        SkepticBreaker foundBreaker = factory.findSkepticBreaker(monitorName);
        assertEquals(createdBreaker, foundBreaker);
    }

    @Test
    public void testFindNonExistentCircuitBreaker() {
        CircuitBreaker foundBreaker = factory.findCircuitBreaker("testNonExistent");
        assertNull(foundBreaker);
    }
    
    @Test
    public void testFindNonExistentSkepticBreaker() {
    	SkepticBreaker foundBreaker = factory.findSkepticBreaker("testNonExistent");
        assertNull(foundBreaker);
    }

    @Test
    public void testGetCircuitBreakerNames() {
        Set<String> testSet = new HashSet<String>();
        testSet.add("one");
        testSet.add("two");
        testSet.add("three");
        testSet.add("four");

        factory.createCircuitBreaker("one", circuitBreakerConfig);
        factory.createCircuitBreaker("two", circuitBreakerConfig);
        factory.createCircuitBreaker("three", circuitBreakerConfig);
        factory.createCircuitBreaker("four", circuitBreakerConfig);
        Set<String> monitorNames = factory.getCircuitBreakerNames();
        assertEquals(testSet, monitorNames);
    }
    
    @Test
    public void testGetSkepticBreakerNames() {
        Set<String> testSet = new HashSet<String>();
        testSet.add("one");
        testSet.add("two");
        testSet.add("three");
        testSet.add("four");

        factory.createSkepticBreaker("one", skepticBreakerConfig);
        factory.createSkepticBreaker("two", skepticBreakerConfig);
        factory.createSkepticBreaker("three", skepticBreakerConfig);
        factory.createSkepticBreaker("four", skepticBreakerConfig);
        Set<String> monitorNames = factory.getSkepticBreakerNames();
        assertEquals(testSet, monitorNames);
    }

    @Test
    public void testEmptyCircuitBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        factory.setCircuitBreakerProperties(overrideProperties);
        CircuitBreaker breaker = factory.createCircuitBreaker("emptyOverrides", circuitBreakerConfig);
        checkCircuitBreaker(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_RESET_MILLIS);
    }
    
    @Test
    public void testEmptySkepticBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        factory.setSkepticBreakerProperties(overrideProperties);
        SkepticBreaker breaker = factory.createSkepticBreaker("emptyOverrides", skepticBreakerConfig);
        checkSkepticBreaker(breaker, TEST_LIMIT, TEST_WINDOW_MILLIS, TEST_GOOD_BASE,
        		TEST_GOOD_MULT, TEST_WAIT_BASE, TEST_WAIT_MULT, TEST_MAX_LEVEL);
    }

    @Test
    public void testCircuitBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        Integer overrideLimit = 10;
        Long overrideResetMillis = 50000L;
        Long overrideWindowMillis = 500000L;
        String name = "testOverrides";

        overrideProperties.put("circuit." + name + ".limit", overrideLimit.toString());
        overrideProperties.put("circuit." + name + ".resetMillis", overrideResetMillis.toString());
        overrideProperties.put("circuit." + name + ".windowMillis", overrideWindowMillis.toString());
        factory.setCircuitBreakerProperties(overrideProperties);

        CircuitBreaker breaker = factory.createCircuitBreaker(name, circuitBreakerConfig);
        checkCircuitBreaker(breaker, overrideLimit,  overrideWindowMillis, overrideResetMillis);
    }
    
    @Test
    public void testSkepticBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        Integer overrideLimit = 10;
        Long overrideWindowMillis = 500000L;
        Long overrideGoodBase = 50000L;
        Long overrideGoodMult = 500000L;
        Long overrideWaitBase = 50000L;
        Long overrideWaitMult = 500000L;
        Long overrideMaxLevel = 25L;
        String name = "testOverrides";

        overrideProperties.put("skeptic." + name + ".limit", overrideLimit.toString());
        overrideProperties.put("skeptic." + name + ".windowMillis", overrideWindowMillis.toString());
        overrideProperties.put("skeptic." + name + ".goodBase", overrideGoodBase.toString());
        overrideProperties.put("skeptic." + name + ".goodMult", overrideGoodMult.toString());
        overrideProperties.put("skeptic." + name + ".waitBase", overrideWaitBase.toString());
        overrideProperties.put("skeptic." + name + ".waitMult", overrideWaitMult.toString());
        overrideProperties.put("skeptic." + name + ".maxLevel", overrideMaxLevel.toString());
        factory.setSkepticBreakerProperties(overrideProperties);

        SkepticBreaker breaker = factory.createSkepticBreaker(name, skepticBreakerConfig);
        checkSkepticBreaker(breaker, overrideLimit,  overrideWindowMillis, overrideGoodBase,
        		overrideGoodMult, overrideWaitBase, overrideWaitMult, overrideMaxLevel);
    }

    @Test
    public void testInvalidCircuitBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        String name = "testOverrides";

        overrideProperties.put("circuit." + name + ".limit", "badLimit");
        overrideProperties.put("circuit." + name + ".resetMillis", "badResetMillis");
        overrideProperties.put("circuit." + name + ".windowMillis", "badWindowMillis");
        factory.setCircuitBreakerProperties(overrideProperties);

        CircuitBreakerConfig emptyConfig =
                new CircuitBreakerConfig(-1, new DefaultFailureInterpreter());
        CircuitBreaker breaker = factory.createCircuitBreaker(name, emptyConfig);
        checkCircuitBreaker(breaker, 0, 0L, 15000L); // These are the CircuitBreaker defaults.
        assertNotNull(breaker);
    }
    
    @Test
    public void testInvalidSkepticBreakerPropertyOverrides() {
        Properties overrideProperties = new Properties();
        String name = "testOverrides";

        overrideProperties.put("skeptic." + name + ".limit", "badLimit");
        overrideProperties.put("skeptic." + name + ".windowMillis", "badWindowMillis");
        overrideProperties.put("skeptic." + name + ".goodBase", "badGoodBase");
        overrideProperties.put("skeptic." + name + ".goodMult", "badGoodMult");
        overrideProperties.put("skeptic." + name + ".waitBase", "badWaitBase");
        overrideProperties.put("skeptic." + name + ".waitMult", "badWaitMult");
        overrideProperties.put("skeptic." + name + ".maxLevel", "badMaxLevel");
        factory.setSkepticBreakerProperties(overrideProperties);

        SkepticBreakerConfig emptyConfig =
                new SkepticBreakerConfig(-1, -1, -1, -1, -1,
                		new DefaultFailureInterpreter());
        SkepticBreaker breaker = factory.createSkepticBreaker(name, emptyConfig);
        checkSkepticBreaker(breaker, 0, 0, 600000L,
        		100L, 1000L, 100L, 20L); // These are the SkepticBreaker Defaults.
        assertNotNull(breaker);
    }

    private void checkCircuitBreaker(CircuitBreaker breaker,
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
    
    private void checkSkepticBreaker(SkepticBreaker breaker, int expectedLimit,
			long expectedWindowMillis, long expectedGoodBase, long expectedGoodMult,
			long expectedWaitBase, long expectedWaitMult, long expectedMaxLevel) {
    	
    	assertNotNull(breaker);

        DefaultFailureInterpreter failureInterpreter = (DefaultFailureInterpreter)breaker.getFailureInterpreter();

        if (failureInterpreter != null) {
            assertEquals(failureInterpreter.getLimit(), expectedLimit);
            assertEquals(failureInterpreter.getWindowMillis(), expectedWindowMillis);
        }
        
        assertEquals(breaker.getGoodBase(), expectedGoodBase);
        assertEquals(breaker.getGoodMult(), expectedGoodMult);
        assertEquals(breaker.getWaitBase(), expectedWaitBase);
        assertEquals(breaker.getWaitMult(), expectedWaitMult);
        assertEquals(breaker.getMaxLevel(), expectedMaxLevel);
	}
    
    private void checkCircuitBreakerNoFailureInterpreter(CircuitBreaker breaker,
            long expectedResetMillis) {

        assertNotNull(breaker);
        assertEquals(breaker.getResetMillis(), expectedResetMillis);
    }
    
    private void checkSkepticBreakerNoFailureInterpreter(SkepticBreaker breaker, 
    		long expectedGoodBase, long expectedGoodMult, long expectedWaitBase, 
    		long expectedWaitMult, long expectedMaxLevel) {
    	assertNotNull(breaker);
		
    	assertEquals(breaker.getGoodBase(), expectedGoodBase);
        assertEquals(breaker.getGoodMult(), expectedGoodMult);
        assertEquals(breaker.getWaitBase(), expectedWaitBase);
        assertEquals(breaker.getWaitMult(), expectedWaitMult);
        assertEquals(breaker.getMaxLevel(), expectedMaxLevel);
	}
}