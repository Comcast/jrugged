package org.fishwife.jrugged.spring.config;

import java.util.ArrayList;
import java.util.List;

import org.fishwife.jrugged.spring.PerformanceMonitorBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


public class MonitorMethodInterceptorDefinitionDecorator implements
                BeanDefinitionDecorator {
    
    public BeanDefinitionHolder decorate(Node source,
                                         BeanDefinitionHolder holder,
                                         ParserContext context) {

        String beanName = holder.getBeanName();
        BeanDefinitionRegistry registry = context.getRegistry();
        registerPerformanceMonitor(beanName, registry);
        registerInterceptor(source, beanName, registry);        
        
        return holder;
    }

    private void registerInterceptor(Node source,
                                     String beanName,
                                     BeanDefinitionRegistry registry) {
        List<String> methodList = buildMethodList(source);
        
        BeanDefinitionBuilder initializer =
            BeanDefinitionBuilder.rootBeanDefinition(SingleServiceWrapperInterceptor.class);
        initializer.addPropertyValue("methods", methodList);
        
        String perfMonitorName = beanName + "PerformanceMonitor";
        initializer.addPropertyReference("serviceWrapper", perfMonitorName);
        
        String interceptorName = beanName + "PerformanceMonitorInterceptor";
        registry.registerBeanDefinition(interceptorName, initializer.getBeanDefinition());
    }

    private List<String> buildMethodList(Node source) {
        Attr attribute = (Attr)source;
        String methods = attribute.getValue();   
        
        String[] methodArray = StringUtils.split(methods, ",");
        
        List<String> methodList = new ArrayList<String>();
        
        for (String methodName : methodArray) {
            methodList.add(methodName.trim());
        }
        return methodList;
    }

    private void registerPerformanceMonitor(String beanName,
                                            BeanDefinitionRegistry registry) {
        
        String perfMonitorName = beanName + "PerformanceMonitor";  
        if (!registry.containsBeanDefinition(perfMonitorName))  {
            BeanDefinitionBuilder initializer =
                BeanDefinitionBuilder.rootBeanDefinition(PerformanceMonitorBean.class);
            registry.registerBeanDefinition(perfMonitorName, initializer.getBeanDefinition());
        }
    }
}
