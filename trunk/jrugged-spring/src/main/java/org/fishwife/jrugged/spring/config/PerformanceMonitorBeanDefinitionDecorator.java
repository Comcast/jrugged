/* PerformanceMonitorBeanDefinitionDecorator.java
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

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


public class PerformanceMonitorBeanDefinitionDecorator implements
                BeanDefinitionDecorator {
    
    public BeanDefinitionHolder decorate(Node source,
                                         BeanDefinitionHolder holder,
                                         ParserContext context) {
        
        boolean enabled = getBooleanAttributeValue(source);
        if (enabled) {
            registerProxyCreator(source, holder, context);
        }
        
        return holder;
    }

    private boolean getBooleanAttributeValue(Node source) {
        Attr attribute = (Attr)source;
        String value = attribute.getValue();
        return "true".equalsIgnoreCase(value);
    }

    private void registerProxyCreator(Node source,
                                      BeanDefinitionHolder holder,
                                      ParserContext context) {
        
        String beanName = holder.getBeanName();
        String proxyName = beanName + "Proxy";
        String interceptorName = beanName + "PerformanceMonitorInterceptor";
        
        BeanDefinitionBuilder initializer =
            BeanDefinitionBuilder.rootBeanDefinition(BeanNameAutoProxyCreator.class);
        
        initializer.addPropertyValue("beanNames", beanName);
        initializer.addPropertyValue("interceptorNames", interceptorName);
        
        BeanDefinitionRegistry registry = context.getRegistry();
        registry.registerBeanDefinition(proxyName, initializer.getBeanDefinition());
    } 
}
