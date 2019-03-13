/* JRuggedNamespaceHandler.java
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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Handler class for the JRugged Spring namespace. This class registers
 * custom parsers and decorators for the new perform element and the
 * perfmon and methods attributes on bean elements.
 *
 * This class is associated with the http://www.fishwife.org/schema/jrugged
 * namespace via the META-INF/spring.handlers file.
 */
public class JRuggedNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * Called by Spring to register any parsers and decorators.
     */
    public void init() {
        registerBeanDefinitionParser("perfmon",
                        new PerformanceMonitorBeanDefinitionParser());

        registerBeanDefinitionDecoratorForAttribute("perfmon",
                        new PerformanceMonitorBeanDefinitionDecorator());

        registerBeanDefinitionDecoratorForAttribute("methods",
                        new MonitorMethodInterceptorDefinitionDecorator());
    }

}
