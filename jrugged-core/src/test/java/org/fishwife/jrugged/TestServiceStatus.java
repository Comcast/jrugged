/* ServiceStatusTest.java
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class TestServiceStatus {

    private ServiceStatus serviceStatus;

    @Test
    public void testServiceStatus() {

        String name = "name";
        Status status = Status.UP;

        serviceStatus = new ServiceStatus(name, status);

        assertEquals(name, serviceStatus.getName());
        assertEquals(status, serviceStatus.getStatus());
        assertNotNull(serviceStatus.getReasons());
        assertTrue(serviceStatus.getReasons().isEmpty());
    }

    @Test
    public void testServiceStatusWithNullReason() {

        String name = "name";
        Status status = Status.UP;

        serviceStatus = new ServiceStatus(name, status, (String)null);

        assertEquals(name, serviceStatus.getName());
        assertEquals(status, serviceStatus.getStatus());
        assertNotNull(serviceStatus.getReasons());
        assertTrue(serviceStatus.getReasons().isEmpty());
    }

    @Test
    public void testServiceStatusWithNullReasons() {

        String name = "name";
        Status status = Status.UP;

        serviceStatus = new ServiceStatus(name, status, (List<String>)null);

        assertEquals(name, serviceStatus.getName());
        assertEquals(status, serviceStatus.getStatus());
        assertNotNull(serviceStatus.getReasons());
        assertTrue(serviceStatus.getReasons().isEmpty());
    }

    @Test
    public void testServiceStatusWithReason() {

        String name = "name";
        Status status = Status.UP;
        String reason = "reason";

        serviceStatus = new ServiceStatus(name, status, reason);

        assertEquals(name, serviceStatus.getName());
        assertEquals(status, serviceStatus.getStatus());
        assertNotNull(serviceStatus.getReasons());
        assertTrue(serviceStatus.getReasons().contains(reason));
    }

    @Test
    public void testServiceStatusWithReasons() {
                String name = "name";
        Status status = Status.UP;
        String reason1 = "reason1";
        String reason2 = "reason2";
        List<String> reasonList = new ArrayList<String>();
        reasonList.add(reason1);
        reasonList.add(reason2);

        serviceStatus = new ServiceStatus(name, status, reasonList);
        assertEquals(name, serviceStatus.getName());
        assertEquals(status, serviceStatus.getStatus());
        assertNotNull(serviceStatus.getReasons());
        assertTrue(serviceStatus.getReasons().contains(reason1));
        assertTrue(serviceStatus.getReasons().contains(reason2));
        assertNotSame(serviceStatus.getReasons(), reasonList);
    }
}
