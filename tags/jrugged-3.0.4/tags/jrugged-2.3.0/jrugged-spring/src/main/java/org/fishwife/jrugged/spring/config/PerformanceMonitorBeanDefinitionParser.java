package org.fishwife.jrugged.spring.config;

import org.fishwife.jrugged.spring.PerformanceMonitorBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;

import org.w3c.dom.Element;

public class PerformanceMonitorBeanDefinitionParser extends
    AbstractSingleBeanDefinitionParser {
    
    protected Class getBeanClass(Element element) {
        return PerformanceMonitorBean.class;
    }
    
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        bean.setLazyInit(false);
    }
    
}
