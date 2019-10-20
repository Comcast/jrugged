/* Copyright 2009-2019 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestRolledUpMonitoredService {

	private RolledUpMonitoredService rolledUpService;

	MonitoredService mockService1;

	MonitoredService mockService2;

	private static final String ROLLEDUP_SERVICE_NAME = "Rolledup Service";
	private static final String SERVICE_1_NAME = "Service 1";
	private static final String SERVICE_2_NAME = "Service 2";

	private static final String SERVICE_1_REASON = "Service 1 Reason";
	private static final String SERVICE_2_REASON = "Service 2 Reason";

	@Before
	public void setUp() {
		mockService1 = createMock(MonitoredService.class);
		mockService2 = createMock(MonitoredService.class);
		List<MonitoredService> criticals = new ArrayList<MonitoredService>();
		criticals.add(mockService1);
		List<MonitoredService> nonCriticals = new ArrayList<MonitoredService>();
		nonCriticals.add(mockService2);

		rolledUpService = new RolledUpMonitoredService(ROLLEDUP_SERVICE_NAME, criticals, nonCriticals);
	}

	@Test
	public void testAllUp() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP));
		expect(mockService2.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_2_NAME, Status.UP));
		replay(mockService1);
		replay(mockService2);

		ServiceStatus serviceStatus = rolledUpService.getServiceStatus();

		assertEquals(ROLLEDUP_SERVICE_NAME, serviceStatus.getName());
		assertEquals(serviceStatus.getStatus(), Status.UP);
	}

	@Test
	public void testCriticalDegraded() {
		expect(mockService1.getServiceStatus())
				.andReturn(new ServiceStatus(SERVICE_1_NAME, Status.DEGRADED, SERVICE_1_REASON));
		expect(mockService2.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_2_NAME, Status.UP));
		replay(mockService1);
		replay(mockService2);

		ServiceStatus serviceStatus = rolledUpService.getServiceStatus();

		assertEquals(ROLLEDUP_SERVICE_NAME, serviceStatus.getName());
		assertEquals(serviceStatus.getStatus(), Status.DEGRADED);
		assertTrue(serviceStatus.getReasons().contains(SERVICE_1_NAME + ":" + SERVICE_1_REASON));
	}

	@Test
	public void testNonCriticalDegraded() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP));
		expect(mockService2.getServiceStatus())
				.andReturn(new ServiceStatus(SERVICE_2_NAME, Status.DEGRADED, SERVICE_2_REASON));
		replay(mockService1);
		replay(mockService2);

		ServiceStatus serviceStatus = rolledUpService.getServiceStatus();

		assertEquals(ROLLEDUP_SERVICE_NAME, serviceStatus.getName());
		assertEquals(serviceStatus.getStatus(), Status.DEGRADED);
		assertTrue(serviceStatus.getReasons().contains(SERVICE_2_NAME + ":" + SERVICE_2_REASON));
	}

	@Test
	public void testCritialAndNonCriticalDegraded() {
		expect(mockService1.getServiceStatus())
				.andReturn(new ServiceStatus(SERVICE_1_NAME, Status.DEGRADED, SERVICE_1_REASON));
		expect(mockService2.getServiceStatus())
				.andReturn(new ServiceStatus(SERVICE_2_NAME, Status.DEGRADED, SERVICE_2_REASON));
		replay(mockService1);
		replay(mockService2);

		ServiceStatus serviceStatus = rolledUpService.getServiceStatus();

		assertEquals(ROLLEDUP_SERVICE_NAME, serviceStatus.getName());
		assertEquals(serviceStatus.getStatus(), Status.DEGRADED);
		assertTrue(serviceStatus.getReasons().contains(SERVICE_1_NAME + ":" + SERVICE_1_REASON));
		assertTrue(serviceStatus.getReasons().contains(SERVICE_2_NAME + ":" + SERVICE_2_REASON));
	}

	@Test
	public void testNonCritialDown() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP));
		expect(mockService2.getServiceStatus())
				.andReturn(new ServiceStatus(SERVICE_2_NAME, Status.DOWN, SERVICE_2_REASON));
		replay(mockService1);
		replay(mockService2);

		ServiceStatus serviceStatus = rolledUpService.getServiceStatus();

		assertEquals(ROLLEDUP_SERVICE_NAME, serviceStatus.getName());
		assertEquals(serviceStatus.getStatus(), Status.DEGRADED);
		assertTrue(serviceStatus.getReasons().contains(SERVICE_2_NAME + ":" + SERVICE_2_REASON));
	}

	@Test
	public void testGetServiceStatusList() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP)).times(2);
		expect(mockService2.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_2_NAME, Status.UP)).times(2);
		replay(mockService1);
		replay(mockService2);

		List<ServiceStatus> statusList = rolledUpService.getAllStatuses();

		assertTrue(statusList.contains(mockService1.getServiceStatus()));
		assertTrue(statusList.contains(mockService2.getServiceStatus()));
	}

	@Test
	public void testGetCriticalServiceStatusList() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP)).times(2);
		expect(mockService2.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_2_NAME, Status.UP)).times(2);
		replay(mockService1);
		replay(mockService2);

		List<ServiceStatus> statusList = rolledUpService.getCriticalStatuses();

		assertTrue(statusList.contains(mockService1.getServiceStatus()));
		assertFalse(statusList.contains(mockService2.getServiceStatus()));
	}

	@Test
	public void testGetNonCriticalServiceStatusList() {
		expect(mockService1.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_1_NAME, Status.UP)).times(2);
		expect(mockService2.getServiceStatus()).andReturn(new ServiceStatus(SERVICE_2_NAME, Status.UP)).times(2);
		replay(mockService1);
		replay(mockService2);

		List<ServiceStatus> statusList = rolledUpService.getNonCriticalStatuses();

		assertFalse(statusList.contains(mockService1.getServiceStatus()));
		assertTrue(statusList.contains(mockService2.getServiceStatus()));
	}
}
