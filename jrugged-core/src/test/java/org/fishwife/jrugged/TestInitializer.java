/* TestInitializer.java
 * 
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestInitializer {
    private Initializable mockClient;
    private Initializer impl;

    @Before
    public void setUp() {
        mockClient = createMock(Initializable.class);
        impl = new Initializer(mockClient);
    }

    @Test
    public void testFirstTimeTrial() throws Exception {
        mockClient.tryInit();
        mockClient.afterInit();
        replay(mockClient);

        impl.initialize();
        Thread.sleep(5);

        verify(mockClient);
        assertTrue(impl.isInitialized());
        assertFalse(impl.isCancelled());
        assertEquals(1, impl.getNumAttempts());
    }

    public void testThirdTimesACharm() throws Exception {
        mockClient.tryInit();
        expectLastCall().andThrow(new RuntimeException()).times(2);
        mockClient.tryInit();
        mockClient.afterInit();
        replay(mockClient);

        impl.setRetryMillis(1);
        impl.initialize();
        Thread.sleep(5);

        assertTrue(impl.isInitialized());
        assertFalse(impl.isCancelled());
        assertEquals(3, impl.getNumAttempts());
        verify(mockClient);
    }

    @Test
    public void testExceededRetries() throws Exception {
        mockClient.tryInit();
        expectLastCall().andThrow(new RuntimeException()).times(2);
        replay(mockClient);

        impl.setMaxRetries(2);
        impl.setRetryMillis(30);
        impl.initialize();
        Thread.sleep(100);

        assertFalse(impl.isInitialized());
        assertFalse(impl.isCancelled());
        assertEquals(2, impl.getNumAttempts());
        verify(mockClient);
    }

    @Test
    public void testExceededRetriesAndCallbackTriggeredSuccessfully() throws Exception {
        mockClient.tryInit();
        expectLastCall().andThrow(new RuntimeException()).times(2);

        mockClient.configuredRetriesMetOrExceededWithoutSuccess();
        replay(mockClient);

        impl.setMaxRetries(2);
        impl.setRetryMillis(3);
        impl.initialize();
        Thread.sleep(10);

        assertFalse(impl.isInitialized());
        assertFalse(impl.isCancelled());
        assertEquals(2, impl.getNumAttempts());
        verify(mockClient);
    }

    @Test
    public void testCancellation() throws Exception {
        mockClient.tryInit();
        expectLastCall().andThrow(new RuntimeException());
        replay(mockClient);

        impl.initialize();
        Thread.sleep(1);
        impl.destroy();

        assertFalse(impl.isInitialized());
        assertTrue(impl.isCancelled());
        assertEquals(1, impl.getNumAttempts());
        verify(mockClient);
    }

}
