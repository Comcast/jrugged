package org.fishwife.jrugged.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class JRuggedNamespaceHandler extends NamespaceHandlerSupport {
    
    public void init() {
        registerBeanDefinitionParser("perfmon",
                        new PerformanceMonitorBeanDefinitionParser()); 
        
        registerBeanDefinitionDecoratorForAttribute("perfmon",
                        new PerformanceMonitorBeanDefinitionDecorator());
        
        registerBeanDefinitionDecoratorForAttribute("methods",
                        new MonitorMethodInterceptorDefinitionDecorator());
    }
    
}
