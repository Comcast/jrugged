/* Copyright 2009-2019 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public final class TestDefaultFailureInterpreter {

	private DefaultFailureInterpreter impl;

	@Before
	public void setUp() {
		impl = new DefaultFailureInterpreter();
	}

	// constructor tests
	@Test
	public void testDefaultConstructor() {

		assertEquals(0, impl.getLimit());
		assertEquals(0, impl.getWindowMillis());

		assertEquals(0, impl.getIgnore().size());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testConstructorWithIgnore() {
		final Class exnClass = RuntimeException.class;
		final Class[] myIgnore = { exnClass };

		impl = new DefaultFailureInterpreter(myIgnore);

		assertEquals(0, impl.getLimit());
		assertEquals(0, impl.getWindowMillis());

		assertEquals(1, impl.getIgnore().size());
		for (Class clazz : impl.getIgnore()) {
			assertSame(clazz, exnClass);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testConstructorWithIgnoreAndTolerance() {
		final Class exnClass = RuntimeException.class;
		final Class[] myIgnore = { exnClass };
		final int frequency = 7777;
		final long time = 1234L;

		impl = new DefaultFailureInterpreter(myIgnore, frequency, time);

		assertEquals(frequency, impl.getLimit());
		assertEquals(time, impl.getWindowMillis());

		assertEquals(1, impl.getIgnore().size());
		for (Class clazz : impl.getIgnore()) {
			assertSame(clazz, exnClass);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testIgnoredExceptionDoesNotTrip() {
		final Class ignoreClass = IOException.class;
		final Class[] myIgnore = { ignoreClass };

		impl.setIgnore(myIgnore);
		assertFalse(impl.shouldTrip(new IOException()));
	}

	@Test
	public void testAnyExceptionTripsByDefault() {
		assertTrue(impl.shouldTrip(new IOException()));
	}

	@Test
	public void testDoesntTripIfFailuresAreWithinTolerance() {
		impl.setLimit(2);
		impl.setWindowMillis(1000);
		Exception exn1 = new Exception();
		Exception exn2 = new Exception();
		boolean result = impl.shouldTrip(exn1);
		assertFalse("this should be false 1", result);
		result = impl.shouldTrip(exn2);
		assertFalse("this should be false 2", result);
	}

	@Test
	public void testTripsIfFailuresExceedTolerance() {
		impl.setLimit(2);
		impl.setWindowMillis(1000);
		assertFalse("this should be false 1", impl.shouldTrip(new Exception()));
		assertFalse("this should be false 2", impl.shouldTrip(new Exception()));
		assertTrue("this should be true 3", impl.shouldTrip(new Exception()));
	}

}
