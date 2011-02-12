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
