/* WebMBeanServerAdapter.java
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
package org.fishwife.jrugged.spring.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.TreeSet;

/**
 * The WebMBeanServerAdapter provides access to MBeans managed by an {@link MBeanServer} via
 * simple string-based accessor methods.  This is particularly useful for implementing a
 * web interface to interact with the MBeans.  Names of MBeans and returned values are sanitized
 * using the {@link MBeanStringSanitizer} to make them HTML-friendly.
 *
 * It should be noted that creating a web interface the JMX beans bypasses the JMX security
 * mechanisms that are built into the JVM.  If there is a need to limit access to the JMX
 * beans then the web interface will need to be secured.
 */
public class WebMBeanServerAdapter {

    private MBeanServer mBeanServer;

    private MBeanStringSanitizer sanitizer;

    /**
     * Constructor.
     * @param mBeanServer the {@link MBeanServer}.
     */
    public WebMBeanServerAdapter(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
        sanitizer = createMBeanStringSanitizer();
    }

    /**
     * Get the {@link Set} of MBean names from the {@link MBeanServer}.  The names are HTML sanitized.
     * @return the {@link Set} of HTML sanitized MBean names.
     */
    public Set<String> getMBeanNames() {
        Set<String> nameSet = new TreeSet<String>();
        for (ObjectInstance instance : mBeanServer.queryMBeans(null, null)) {
            nameSet.add(sanitizer.escapeValue(instance.getObjectName().getCanonicalName()));
        }
        return nameSet;
    }

    /**
     * Create a WebMBeanAdaptor for a specified MBean name.
     * @param mBeanName the MBean name (can be URL-encoded).
     * @param encoding the string encoding to be used (i.e. UTF-8)
     * @return the created WebMBeanAdaptor.
     * @throws JMException Java Management Exception
     * @throws UnsupportedEncodingException if the encoding is not supported.
     */
    public WebMBeanAdapter createWebMBeanAdapter(String mBeanName, String encoding)
            throws JMException, UnsupportedEncodingException {
        return new WebMBeanAdapter(mBeanServer, mBeanName, encoding);
    }

    MBeanStringSanitizer createMBeanStringSanitizer() {
        return new MBeanStringSanitizer();
    }
}
