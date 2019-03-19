/* MonitorMethodInterceptorDefinitionDecorator.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

/**
 * This class is invoked when Spring encounters the jrugged:methods attribute on
 * a bean. It parses the attribute value with is a comma delimited list of
 * method names to wrap with the PerformanceMonitor. It tells Spring to create a
 * SingleServiceWrapperInterceptor named after the bean with
 * "PerformanceMonitorInterceptor" appended to the name. It also defines a
 * PerformanceMonitorBean named after the bean with "PerformanceMonitor"
 * appended to it. The interceptor has a reference to the PerformanceMonitor.
 */
public class MonitorMethodInterceptorDefinitionDecorator implements BeanDefinitionDecorator {

	/**
	 * Method called by Spring when it encounters the custom jrugged:methods
	 * attribute. Registers the performance monitor and interceptor.
	 */
	public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder holder, ParserContext context) {

		String beanName = holder.getBeanName();
		BeanDefinitionRegistry registry = context.getRegistry();
		registerPerformanceMonitor(beanName, registry);
		registerInterceptor(source, beanName, registry);

		return holder;
	}

	/**
	 * Register a new SingleServiceWrapperInterceptor for the bean being wrapped,
	 * associate it with the PerformanceMonitor and tell it which methods to
	 * intercept.
	 *
	 * @param source   An Attribute node from the spring configuration
	 * @param beanName The name of the bean that this performance monitor is wrapped
	 *                 around
	 * @param registry The registry where all the spring beans are registered
	 */
	private void registerInterceptor(Node source, String beanName, BeanDefinitionRegistry registry) {
		List<String> methodList = buildMethodList(source);

		BeanDefinitionBuilder initializer = BeanDefinitionBuilder
				.rootBeanDefinition(SingleServiceWrapperInterceptor.class);
		initializer.addPropertyValue("methods", methodList);

		String perfMonitorName = beanName + "PerformanceMonitor";
		initializer.addPropertyReference("serviceWrapper", perfMonitorName);

		String interceptorName = beanName + "PerformanceMonitorInterceptor";
		registry.registerBeanDefinition(interceptorName, initializer.getBeanDefinition());
	}

	/**
	 * Parse the jrugged:methods attribute into a List of strings of method names
	 *
	 * @param source An Attribute node from the spring configuration
	 *
	 * @return List&lt;String&gt;
	 */
	private List<String> buildMethodList(Node source) {
		Attr attribute = (Attr) source;
		String methods = attribute.getValue();
		return parseMethodList(methods);
	}

	/**
	 * Parse a comma-delimited list of method names into a List of strings.
	 * Whitespace is ignored.
	 *
	 * @param methods the comma delimited list of methods from the spring
	 *                configuration
	 *
	 * @return List&lt;String&gt;
	 */
	public List<String> parseMethodList(String methods) {

		String[] methodArray = StringUtils.delimitedListToStringArray(methods, ",");

		List<String> methodList = new ArrayList<String>();

		for (String methodName : methodArray) {
			methodList.add(methodName.trim());
		}
		return methodList;
	}

	/**
	 * Register a new PerformanceMonitor with Spring if it does not already exist.
	 *
	 * @param beanName The name of the bean that this performance monitor is wrapped
	 *                 around
	 * @param registry The registry where all the spring beans are registered
	 */
	private void registerPerformanceMonitor(String beanName, BeanDefinitionRegistry registry) {

		String perfMonitorName = beanName + "PerformanceMonitor";
		if (!registry.containsBeanDefinition(perfMonitorName)) {
			BeanDefinitionBuilder initializer = BeanDefinitionBuilder.rootBeanDefinition(PerformanceMonitorBean.class);
			registry.registerBeanDefinition(perfMonitorName, initializer.getBeanDefinition());
		}
	}
}
