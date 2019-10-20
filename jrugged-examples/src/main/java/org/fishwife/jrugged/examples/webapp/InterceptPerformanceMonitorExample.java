/* InterceptPerformanceMonitorExample.java
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

import org.fishwife.jrugged.examples.InterceptResponseTweaker;
import org.fishwife.jrugged.spring.PerformanceMonitorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Controller()
public class InterceptPerformanceMonitorExample {
	@Autowired
	private PerformanceMonitorBean performanceBean;

	public PerformanceMonitorBean getPerformanceBean() {
		return performanceBean;
	}

	public void setPerformanceBean(PerformanceMonitorBean performanceBean) {
		this.performanceBean = performanceBean;
	}

	@Autowired
	private InterceptResponseTweaker interceptResponseTweaker;

	public InterceptResponseTweaker getResponseTweaker() {
		return interceptResponseTweaker;
	}

	public void setResponseTweaker(InterceptResponseTweaker interceptResponseTweaker) {
		this.interceptResponseTweaker = interceptResponseTweaker;
	}

	@RequestMapping("/interceptPerformanceMonitor")
	public ModelAndView viewMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int delayedFor = interceptResponseTweaker.delay();
		ModelAndView view = new ModelAndView("interceptPerf-monitor");
		view.addObject("delay", new Integer(delayedFor));
		return view;
	}

	@RequestMapping("/interceptPerformanceMonitor/stats")
	public ModelAndView viewPerformanceMonitor(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		final StringBuilder sb = new StringBuilder();

		// Go through all methods and invoke those with ManagedAttribute
		// marker annotations
		Method[] methods = performanceBean.getClass().getMethods();
		for (Method monitorMethod : methods) {
			if (monitorMethod.getName().startsWith("get")) {
				sb.append(String.format("\t%s: %s\n", monitorMethod.getName().substring(3),
						monitorMethod.invoke(performanceBean, new Object[] {})));
			}
		}
		sb.append("\n");

		response.setContentType("text/plain");
		response.getWriter().println(sb.toString());
		return null;
	}
}
