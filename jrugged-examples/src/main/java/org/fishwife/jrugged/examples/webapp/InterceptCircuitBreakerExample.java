/* InterceptCircuitBreakerExample.java
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
package org.fishwife.jrugged.examples.webapp;

import org.fishwife.jrugged.examples.BreakerResponseTweaker;
import org.fishwife.jrugged.spring.CircuitBreakerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Controller()
public class InterceptCircuitBreakerExample {
	@Autowired
	private CircuitBreakerBean circuitBreakerBean;

	public CircuitBreakerBean getCircuitBreakerBean() {
		return circuitBreakerBean;
	}

	public void setCircuitBreakerBean(CircuitBreakerBean circuitBreakerBean) {
		this.circuitBreakerBean = circuitBreakerBean;
	}

	@Autowired
	private BreakerResponseTweaker breakerResponseTweaker;

	public BreakerResponseTweaker getResponseTweaker() {
		return breakerResponseTweaker;
	}

	public void setResponseTweaker(BreakerResponseTweaker breakerResponseTweaker) {
		this.breakerResponseTweaker = breakerResponseTweaker;
	}

	@RequestMapping("/interceptCircuitBreaker")
	public ModelAndView viewMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int delayedFor = breakerResponseTweaker.delay();
		ModelAndView view = new ModelAndView("interceptBreaker");
		view.addObject("delay", new Integer(delayedFor));
		return view;
	}

	@RequestMapping("/interceptCircuitBreaker/stats")
	public ModelAndView viewPerformanceMonitor(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		final StringBuilder sb = new StringBuilder();

		// Go through all methods and invoke those with ManagedAttribute
		// marker annotations
		Method[] methods = circuitBreakerBean.getClass().getMethods();
		for (Method monitorMethod : methods) {
			if (monitorMethod.getName().startsWith("get")) {
				sb.append(String.format("\t%s: %s\n", monitorMethod.getName().substring(3),
						monitorMethod.invoke(circuitBreakerBean, new Object[] {})));
			}
		}
		sb.append("\n");

		response.setContentType("text/plain");
		response.getWriter().println(sb.toString());
		return null;
	}
}
