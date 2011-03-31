/* TestRolledUpStatus.java
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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class TestRolledUpStatus {
    private RolledUpStatus impl;
    private Monitorable mockCritical;
    private Monitorable mockNoncritical;

    @Before
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

    @Test
    public void testAllSystemsGo() {
        expect(mockCritical.getStatus()).andReturn(Status.UP);
        expect(mockNoncritical.getStatus()).andReturn(Status.UP);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.UP, result);
        verifyMocks();
    }

    @Test
    public void testNoncriticalYellowGivesYellow() {
        expect(mockCritical.getStatus()).andReturn(Status.UP);
        expect(mockNoncritical.getStatus()).andReturn(Status.DEGRADED);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.DEGRADED, result);
        verifyMocks();
    }

    @Test
    public void testNoncriticalRedGivesYellow() {
        expect(mockCritical.getStatus()).andReturn(Status.UP);
        expect(mockNoncritical.getStatus()).andReturn(Status.DOWN);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.DEGRADED, result);
        verifyMocks();
    }

    @Test
    public void testCriticalYellowGivesYellow() {
        expect(mockCritical.getStatus()).andReturn(Status.DEGRADED);
        expect(mockNoncritical.getStatus()).andReturn(Status.UP);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.DEGRADED, result);
        verifyMocks();
    }

    @Test
    public void testCriticalRedGivesRed() {
        expect(mockCritical.getStatus()).andReturn(Status.DOWN);
        expect(mockNoncritical.getStatus()).andReturn(Status.UP);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.DOWN, result);
        verifyMocks();
    }

    @Test
    public void testNoncriticalYellowAndCriticalRedGivesRed() {
        expect(mockCritical.getStatus()).andReturn(Status.DOWN);
        expect(mockNoncritical.getStatus()).andReturn(Status.DEGRADED);
        replayMocks();

        Status result = impl.getStatus();

        assertEquals(Status.DOWN, result);
        verifyMocks();
    }

    @Test
    public void onlyNonGreenSystemsAreReturned(){
        expect(mockCritical.getStatus()).andReturn(Status.DOWN);
        expect(mockNoncritical.getStatus()).andReturn(Status.UP);


        replayMocks();

        Collection<?extends Monitorable> nonGreen = impl.getNonGreenSystems();

        assertEquals(1, nonGreen.size());
        verifyMocks();
    }
}
