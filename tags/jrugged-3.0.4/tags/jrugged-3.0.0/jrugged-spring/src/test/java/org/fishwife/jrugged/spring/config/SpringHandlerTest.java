/* SpringHandlerTest.java
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
package org.fishwife.jrugged.spring.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.fishwife.jrugged.PerformanceMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class SpringHandlerTest {
    
    ClassPathXmlApplicationContext context;

    @Before
    public void setUp() {
        String[] config = new String[] {"applicationContext.xml"};
        context = new ClassPathXmlApplicationContext(config);
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void testAttributesCreatePerfMon() {
        
        DummyService service = (DummyService)context.getBean("dummyService");
        assertNotNull(service);

        PerformanceMonitor monitor =
            (PerformanceMonitor)context.getBean("dummyServicePerformanceMonitor");
        assertNotNull(monitor);        
        
        service.foo();  
        assertEquals(1, monitor.getRequestCount());
        
        service.bar();
        assertEquals(2, monitor.getRequestCount());

        service.baz();
        assertEquals(2, monitor.getRequestCount());        
        
        MethodInterceptor wrapper =
            (MethodInterceptor)context.getBean("dummyServicePerformanceMonitorInterceptor");
        assertNotNull(wrapper);   
    }

    @Test
    public void testPerfMonElementCreatedPerfMon() {
        PerformanceMonitor monitor =
            (PerformanceMonitor)context.getBean("performanceMonitor");
        assertNotNull(monitor);            
    }
}
