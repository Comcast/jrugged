package org.fishwife.jrugged;

import java.util.Arrays;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

public class TestRolledUpStatus extends TestCase {
    private RolledUpStatus impl;
    private Monitorable mockCritical;
    private Monitorable mockNoncritical;

    public void setUp() {
	mockCritical = createMock(Monitorable.class);
	mockNoncritical = createMock(Monitorable.class);
	impl = new RolledUpStatus(Arrays.asList(mockCritical),
				  Arrays.asList(mockNoncritical));
    }

    public void replayMocks() {
	replay(mockCritical);
	replay(mockNoncritical);
    }

    public void verifyMocks() {
	verify(mockCritical);
	verify(mockNoncritical);
    }

    public void testAllSystemsGo() {
	expect(mockCritical.getStatus()).andReturn(Status.UP);
	expect(mockNoncritical.getStatus()).andReturn(Status.UP);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.UP, result);
	verifyMocks();
    }

    public void testNoncriticalYellowGivesYellow() {
	expect(mockCritical.getStatus()).andReturn(Status.UP);
	expect(mockNoncritical.getStatus()).andReturn(Status.DEGRADED);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.DEGRADED, result);
	verifyMocks();
    }

    public void testNoncriticalRedGivesYellow() {
	expect(mockCritical.getStatus()).andReturn(Status.UP);
	expect(mockNoncritical.getStatus()).andReturn(Status.DOWN);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.DEGRADED, result);
	verifyMocks();
    }

    public void testCriticalYellowGivesYellow() {
	expect(mockCritical.getStatus()).andReturn(Status.DEGRADED);
	expect(mockNoncritical.getStatus()).andReturn(Status.UP);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.DEGRADED, result);
	verifyMocks();
    }

    public void testCriticalRedGivesRed() {
	expect(mockCritical.getStatus()).andReturn(Status.DOWN);
	expect(mockNoncritical.getStatus()).andReturn(Status.UP);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.DOWN, result);
	verifyMocks();
    }

    public void testNoncriticalYellowAndCriticalRedGivesRed() {
	expect(mockCritical.getStatus()).andReturn(Status.DOWN);
	expect(mockNoncritical.getStatus()).andReturn(Status.DEGRADED);
	replayMocks();

	Status result = impl.getStatus();

	assertEquals(Status.DOWN, result);
	verifyMocks();
    }
}