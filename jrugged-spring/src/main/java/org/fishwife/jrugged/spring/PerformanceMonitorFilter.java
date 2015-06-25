/* PerformanceMonitorFilter.java
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
package org.fishwife.jrugged.spring;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.fishwife.jrugged.WrappedException;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This class is a standard Servlet filter that can be configured in web.xml to wrap all
 * request handling in a {@link org.fishwife.jrugged.PerformanceMonitor}. In order to get
 * useful access to the statistics, however, it is most convenient to make use of Spring's
 * {@link org.springframework.web.filter.DelegatingFilterProxy} in <code>web.xml</code> and
 * instantiate this filter within a Spring application context. This will allow the JMX
 * annotations inherited from {@link PerformanceMonitorBean} to take effect, with the result
 * that you can get a high-level performance monitor wrapped around all of your application's
 * request handling.
 */
@ManagedResource
public class PerformanceMonitorFilter extends PerformanceMonitorBean implements Filter {

    public void doFilter(final ServletRequest req, final ServletResponse resp,
            final FilterChain chain) throws IOException, ServletException {
        try {
            invoke(new Runnable() {
                public void run() {
                    try {
                        chain.doFilter(req, resp);
                    } catch (IOException e) {
                        throw new WrappedException(e);
                    } catch (ServletException e) {
                        throw new WrappedException(e);
                    }
                }
            });
        } catch (WrappedException e) {
            Throwable wrapped = e.getCause();
            if (wrapped instanceof IOException) {
                throw (IOException)wrapped;
            } else if (wrapped instanceof ServletException) {
                throw (ServletException)wrapped;
            } else {
                throw new IllegalStateException("unknown wrapped exception", wrapped);
            }
        } catch (Exception e) {
            throw new IllegalStateException("unknown checked exception", e);
        }
    }

    public void init(FilterConfig config) throws ServletException { }

    public void destroy() { }
}
