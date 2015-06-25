/* PerformanceMonitorBeanDefinitionDecorator.java
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
package org.fishwife.jrugged.spring.config;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * This class is invoked when Spring encounters the jrugged:perfmon attribute.
 * If the attribute is set to true, then it registers a BeanNameAutoProxyCreator
 * that gets associated with the SingleServiceWrapperInterceptor created by
 * the jrugged:methods attribute.
 */
public class PerformanceMonitorBeanDefinitionDecorator implements
                BeanDefinitionDecorator {

    /**
     * Method called by Spring when it encounters the jrugged:perfmon attribute.
     * Checks if the attribute is true, and if so, it registers a proxy for the
     * bean.
     */
    public BeanDefinitionHolder decorate(Node source,
                                         BeanDefinitionHolder holder,
                                         ParserContext context) {

        boolean enabled = getBooleanAttributeValue(source);
        if (enabled) {
            registerProxyCreator(source, holder, context);
        }

        return holder;
    }

    /**
     * Gets the value of an attribute and returns true if it is set to "true"
     * (case-insensitive), otherwise returns false.
     *
     * @param source An Attribute node from the spring configuration
     *
     * @return boolean
     */
    private boolean getBooleanAttributeValue(Node source) {
        Attr attribute = (Attr)source;
        String value = attribute.getValue();
        return "true".equalsIgnoreCase(value);
    }

    /**
     * Registers a BeanNameAutoProxyCreator class that wraps the bean being
     * monitored. The proxy is associated with the PerformanceMonitorInterceptor
     * for the bean, which is created when parsing the methods attribute from
     * the springconfiguration xml file.
     *
     * @param source An Attribute node from the spring configuration
     * @param holder A container for the beans I will create
     * @param context the context currently parsing my spring config
     */
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
