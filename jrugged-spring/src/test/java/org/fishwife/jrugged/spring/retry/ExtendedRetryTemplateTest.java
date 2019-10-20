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

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryState;

public class ExtendedRetryTemplateTest {
	@Test
	public void test_asCallable_callable() throws Exception {
		Callable<Long> callable = Mockito.mock(Callable.class);
		ExtendedRetryTemplate template = new ExtendedRetryTemplate();

		Mockito.when(callable.call()).thenReturn(10L);

		Callable<Long> wrapped = template.asCallable(callable);
		Assert.assertEquals(10L, wrapped.call().longValue());

		Mockito.verify(callable, Mockito.times(1)).call();
	}

	@Test
	public void test_asCallable_callback() throws Exception {
		RetryCallback<Long, Exception> callback = Mockito.mock(RetryCallback.class);
		ExtendedRetryTemplate template = new ExtendedRetryTemplate();

		Mockito.when(callback.doWithRetry(Mockito.any(RetryContext.class))).thenReturn(10L);

		Callable<Long> wrapped = template.asCallable(callback);
		Assert.assertEquals(10L, wrapped.call().longValue());

		Mockito.verify(callback, Mockito.times(1)).doWithRetry(Mockito.any(RetryContext.class));
	}

	@Test
	public void test_execute_callable() throws Exception {
		Callable<Long> callable = Mockito.mock(Callable.class);
		ExtendedRetryTemplate template = new ExtendedRetryTemplate();

		Mockito.when(callable.call()).thenReturn(10L);

		Assert.assertEquals(10L, template.execute(callable).longValue());

		Mockito.verify(callable, Mockito.times(1)).call();

	}

	@Test
	public void test_execute_callableWithState() throws Exception {
		Callable<Long> callable = Mockito.mock(Callable.class);
		RetryState retryState = Mockito.mock(RetryState.class);
		ExtendedRetryTemplate template = new ExtendedRetryTemplate();

		Mockito.when(callable.call()).thenReturn(10L);

		Assert.assertEquals(10L, template.execute(callable, retryState).longValue());

		Mockito.verify(callable, Mockito.times(1)).call();

	}

	@Test
	public void test_execute_callableWithRecoveryAndState() throws Exception {
		Callable<Long> callable = Mockito.mock(Callable.class);
		RetryState retryState = Mockito.mock(RetryState.class);
		RecoveryCallback<Long> recoveryCallback = Mockito.mock(RecoveryCallback.class);

		ExtendedRetryTemplate template = new ExtendedRetryTemplate();

		Mockito.when(callable.call()).thenReturn(10L);

		Assert.assertEquals(10L, template.execute(callable, recoveryCallback, retryState).longValue());

		Mockito.verify(callable, Mockito.times(1)).call();
	}
}
