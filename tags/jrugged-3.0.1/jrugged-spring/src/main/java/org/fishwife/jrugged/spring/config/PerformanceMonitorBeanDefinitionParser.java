/* PerformanceMonitorBeanDefinitionParser.java
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

import org.fishwife.jrugged.spring.PerformanceMonitorBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;

import org.w3c.dom.Element;

/**
 * Simple BeanDefinitionParser that creates beans that are instances of the
 * PerformanceMonitorBean class. Spring already provides an
 * AbstractSingleBeanDefinitionParser that handles most of the work to do this.
 */
public class PerformanceMonitorBeanDefinitionParser extends
    AbstractSingleBeanDefinitionParser {
    
    /**
     * Return the class to instantiate. In this case it is PerformanceMonitorBean.
     */
    protected Class getBeanClass(Element element) {
        return PerformanceMonitorBean.class;
    }
    
    /**
     * Disables lazy loading of the bean.
     */
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        bean.setLazyInit(false);
    }
}
