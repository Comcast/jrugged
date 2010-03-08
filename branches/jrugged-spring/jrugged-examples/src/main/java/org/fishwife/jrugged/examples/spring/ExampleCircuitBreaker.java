/* ExampleCircuitBreaker.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.examples.spring;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.spring.ServiceWrapperInterceptor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ExampleCircuitBreaker implements Runnable {

    ServiceWrapperInterceptor serviceWrapperInterceptor;
    CircuitBreaker exampleCircuitBreaker;
    ApplicationContext ctx;

    public void setup() throws Exception {
        ctx = new ClassPathXmlApplicationContext(new String[] {"springExampleContext.xml"});

        serviceWrapperInterceptor = (ServiceWrapperInterceptor) ctx.getBean("exampleCircuitBreakerInterceptor");
    }

    public void exampleCircuitOne() throws Exception {
        ExampleService service = (ExampleService) ctx.getBean("exampleCircuitService");

        service.exampleMethodCallOne();
    }

    public void run() {
        while (1==1) {
            try {
                exampleCircuitOne();
            }
            catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) throws Exception {
        ExampleCircuitBreaker tcb = new ExampleCircuitBreaker();
        tcb.setup();

        new Thread(tcb).start();
    }
}
