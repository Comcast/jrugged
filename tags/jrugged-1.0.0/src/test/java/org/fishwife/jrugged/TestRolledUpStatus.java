/* TestRolledUpStatus.java
 * 
 * Copyright (C) 2009 Jonathan T. Moore
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