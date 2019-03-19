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

package org.fishwife.jrugged.spring.retry;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExtendedPredicatesTest {

	@Test
	public void test_isInstanceOf() {
		Predicate<Throwable> t = ExtendedPredicates.isInstanceOf(Throwable.class, RuntimeException.class);
		Assert.assertTrue(t.apply(new RuntimeException()));
		Assert.assertFalse(t.apply(new IOException()));
	}

	@Test
	public void test_throwableContainsMessage_sensitive() {
		Predicate<Throwable> t = ExtendedPredicates.throwableContainsMessage("foo", true);
		Assert.assertTrue(t.apply(new RuntimeException("foo")));
		Assert.assertFalse(t.apply(new RuntimeException("Foo")));
		Assert.assertFalse(t.apply(new RuntimeException("bar")));
		Assert.assertFalse(t.apply(new RuntimeException("Bar")));
	}

	@Test
	public void test_throwableContainsMessage_insensitive() {
		Predicate<Throwable> t = ExtendedPredicates.throwableContainsMessage("foo", false);
		Assert.assertTrue(t.apply(new RuntimeException("Foo")));
		Assert.assertTrue(t.apply(new RuntimeException("foo")));
		Assert.assertFalse(t.apply(new RuntimeException("bar")));
		Assert.assertFalse(t.apply(new RuntimeException("Bar")));
	}

}
