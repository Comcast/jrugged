/* TestConstantFlowRegulator.java
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
package org.fishwife.jrugged;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConstantFlowRegulator {
	private ConstantFlowRegulator impl;
	private Callable<Object> mockCallable;
	private Runnable mockRunnable;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		impl = new ConstantFlowRegulator();
		mockCallable = createMock(Callable.class);
		mockRunnable = createMock(Runnable.class);
	}

	@Test
	public void testConfiguration() {
		impl.setRequestPerSecondThreshold(50);

		assertEquals(50, impl.getRequestPerSecondThreshold());
	}

	@Test
	public void testRPSThresholdDefault() {
		assertEquals(-1, impl.getRequestPerSecondThreshold());
	}

	@Test
	public void testFlowThrottlerCanProceedWhenThresholdIsDefault() {
		assertTrue(impl.canProceed());
	}

	@Test
	public void testCanProceedWhenThresholdLessThanConfigured() {
		impl.setRequestPerSecondThreshold(50);

		assertTrue(impl.canProceed());
	}

	@Test(expected = Exception.class)
	public void testThrowsExceptionWhenCallableCanNotProceed() throws Exception {
		final Object obj = new Object();

		expect(mockCallable.call()).andReturn(obj);
		expectLastCall().anyTimes();
		replay(mockCallable);

		impl.setRequestPerSecondThreshold(1);

		for (int i = 0; i < 5; i++) {
			try {
				impl.invoke(mockCallable);
			} catch (FlowRateExceededException e) {
				System.out.println(e.getMessage());
				// Ignore these
			}
		}

		impl.invoke(mockCallable);

		verify(mockCallable);
	}

	public class DomainException extends Exception {
	}

	public class TestConstantFlowRegulatorExceptionMapper
			implements ConstantFlowRegulatorExceptionMapper<DomainException> {
		public DomainException map(ConstantFlowRegulator flowRegulator, FlowRateExceededException e) {
			return new DomainException();
		}
	}

	@Test(expected = DomainException.class)
	public void testThrowsMappedExceptionWhenCanNotProceed() throws Exception {
		impl.setRequestPerSecondThreshold(1);
		impl.setExceptionMapper(new TestConstantFlowRegulatorExceptionMapper());
		for (int i = 0; i < 5; i++) {
			impl.invoke(mockCallable);
		}
	}

	@Test(expected = Exception.class)
	public void testThrowsExceptionWhenRunnableCanNotProceed() throws Exception {
		impl.setRequestPerSecondThreshold(1);
		for (int i = 0; i < 5; i++) {
			impl.invoke(mockRunnable);
		}
	}

	@Test(expected = DomainException.class)
	public void testThrowsMappedExceptionWhenCanNotProceedViaConstructor() throws Exception {
		ConstantFlowRegulator impl = new ConstantFlowRegulator(1, new TestConstantFlowRegulatorExceptionMapper());
		for (int i = 0; i < 5; i++) {
			impl.invoke(mockCallable);
		}
	}

}
