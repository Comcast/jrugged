/* StatusController.java
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fishwife.jrugged.MonitoredService;
import org.fishwife.jrugged.Status;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This is a convenient {@link Controller} that can be used to implement a
 * "heartbeat" URL in a web application. The <code>StatusController</code>
 * has a particular {@link MonitoredService} associated with it (a common use
 * case would be to inject a {@link org.fishwife.jrugged.RolledUpMonitoredService} for
 * overall system health. The <code>StatusController</code> writes the
 * current status out in the response body and sets an appropriate HTTP
 * response code. This is useful in a load balancer setting where the load
 * balancer periodically pings a pool of application servers to see if they
 * are "OK" and removes them from the pool if they are not. If the
 * <code>Monitorable</code> is capable of serving requests (i.e. is GREEN
 * or YELLOW) then we return a 2XX response code; otherwise we return a 5XX
 * response code.
 */
public class StatusController implements Controller {

    private static Map<Status,Integer> responseCodeMap;
    static {
        responseCodeMap = new HashMap<Status,Integer>();
        responseCodeMap.put(Status.FAILED, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseCodeMap.put(Status.INIT, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseCodeMap.put(Status.DOWN, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseCodeMap.put(Status.DEGRADED, HttpServletResponse.SC_OK);
        responseCodeMap.put(Status.BYPASS, HttpServletResponse.SC_OK);
        responseCodeMap.put(Status.UP, HttpServletResponse.SC_OK);
    }

    private MonitoredService monitoredService;

    public StatusController(MonitoredService monitoredService) {
        this.monitoredService = monitoredService;
    }

    public ModelAndView handleRequest(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        Status currentStatus = monitoredService.getServiceStatus().getStatus();
        setResponseCode(currentStatus, resp);
        setAppropriateWarningHeaders(resp, currentStatus);
        setCachingHeaders(resp);
        writeOutCurrentStatusInResponseBody(resp, currentStatus);
        return null;
    }

    private void setCachingHeaders(HttpServletResponse resp) {
        long now = System.currentTimeMillis();
        resp.setDateHeader("Date", now);
        resp.setDateHeader("Expires", now);
        resp.setHeader("Cache-Control","no-cache");
    }

    private void setAppropriateWarningHeaders(HttpServletResponse resp,
            Status currentStatus) {
        if (Status.DEGRADED.equals(currentStatus)) {
            resp.addHeader("Warning", "199 jrugged \"Status degraded\"");
        }
    }

    private void writeOutCurrentStatusInResponseBody(HttpServletResponse resp,
            Status currentStatus) throws IOException {
        resp.setHeader("Content-Type","text/plain;charset=utf-8");
        String body = currentStatus + "\n";
        byte[] bytes = body.getBytes();
        resp.setHeader("Content-Length", bytes.length + "");
        OutputStream out = resp.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
    }

    private void setResponseCode(Status currentStatus, HttpServletResponse resp) {
        if (responseCodeMap.containsKey(currentStatus)) {
            resp.setStatus(responseCodeMap.get(currentStatus));
        }
    }

}
