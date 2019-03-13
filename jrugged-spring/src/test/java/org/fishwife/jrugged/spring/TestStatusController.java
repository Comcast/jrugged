/* TestStatusController.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

import org.fishwife.jrugged.Status;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class TestStatusController {

    private MonitoredServiceStub monitoredService;
    private StatusController impl;
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;

    @Before
    public void setUp() {
        monitoredService = new MonitoredServiceStub();
        impl = new StatusController(monitoredService);
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
    }

    private void assertResponseCodeIs(Status status, int code) throws Exception {
        monitoredService.setStatus(status);
        impl.handleRequest(req, resp);
        assertEquals(code, resp.getStatus());
    }

    private void assertBodyForStatusIs(Status status, String bodyString)
        throws Exception {
        monitoredService.setStatus(status);
        impl.handleRequest(req, resp);
        assertEquals(bodyString, resp.getContentAsString());
        assertEquals("text/plain;charset=utf-8", resp.getHeader("Content-Type"));
        assertEquals(bodyString.getBytes().length + "", resp.getHeader("Content-Length"));
    }

    @Test
    public void handlesRequestInternally() throws Exception {
        assertNull(impl.handleRequest(req, resp));
    }

    @Test
    public void returns200IfStatusIsUp() throws Exception {
        assertResponseCodeIs(Status.UP, 200);
    }

    @Test
    public void returns503IfStatusIsDown() throws Exception {
        assertResponseCodeIs(Status.DOWN, 503);
    }

    @Test
    public void returns200IfStatusIsDegraded() throws Exception {
        assertResponseCodeIs(Status.DEGRADED, 200);
    }

    @Test
    public void setsWarningHeaderIfDegraded() throws Exception {
        monitoredService.setStatus(Status.DEGRADED);
        impl.handleRequest(req, resp);
        boolean found = false;
        for(Object val : resp.getHeaders("Warning")) {
            if ("199 jrugged \"Status degraded\"".equals(val)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void returns200IfStatusIsBypass() throws Exception {
        assertResponseCodeIs(Status.BYPASS, 200);
    }

    @Test
    public void returns500IfStatusIsFailed() throws Exception {
        assertResponseCodeIs(Status.FAILED, 500);
    }

    @Test
    public void returns503IfStatusIsInit() throws Exception {
        assertResponseCodeIs(Status.INIT, 503);
    }

    @Test
    public void writesStatusOutInResponseBodyWhenUp() throws Exception {
        assertBodyForStatusIs(Status.UP, "UP\n");
    }

    @Test
    public void writesStatusOutInResponseBodyWhenDown() throws Exception {
        assertBodyForStatusIs(Status.DOWN, "DOWN\n");
    }

    @Test
    public void setsNonCacheableHeaders() throws Exception {
        impl.handleRequest(req,resp);
        assertNotNull(resp.getHeader("Expires"));
        assertEquals(resp.getHeader("Date"), resp.getHeader("Expires"));
        assertEquals("no-cache", resp.getHeader("Cache-Control"));
    }

}
