package org.fishwife.jrugged;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestCircuitBreakerConfig {
    private FailureInterpreter mockFailureInterpreter;
    private long resetMillis;
    private Clock mockClock;

    @Before
    public void setUp() {
        resetMillis = 1000L;
        mockFailureInterpreter = createMock(FailureInterpreter.class);
        mockClock = createMock(Clock.class);
    }

    @Test
    public void testConstructConfigWithOptionalClock() {
        CircuitBreakerConfig underTest = new CircuitBreakerConfig(resetMillis, mockFailureInterpreter, mockClock);

        assertEquals(resetMillis, underTest.getResetMillis());
        assertSame(mockFailureInterpreter, underTest.getFailureInterpreter());
        assertSame(mockClock, underTest.getClock());
    }

    @Test
    public void testConstructConfigWithoutOptionalClock() {
        CircuitBreakerConfig underTest = new CircuitBreakerConfig(resetMillis, mockFailureInterpreter);

        assertEquals(resetMillis, underTest.getResetMillis());
        assertSame(mockFailureInterpreter, underTest.getFailureInterpreter());
        assertNull(underTest.getClock());
    }
}
