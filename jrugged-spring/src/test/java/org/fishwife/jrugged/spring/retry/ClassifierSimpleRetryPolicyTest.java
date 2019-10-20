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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryContext;

public class ClassifierSimpleRetryPolicyTest {
	@Test
	public void test_classify() {
		RetryContext context = Mockito.mock(RetryContext.class);
		Classifier<Throwable, Boolean> classifier = Mockito.mock(Classifier.class);
		ClassifierSimpleRetryPolicy policy = new ClassifierSimpleRetryPolicy(classifier);
		Mockito.when(context.getLastThrowable()).thenReturn(new RuntimeException());
		Mockito.when(classifier.classify(Mockito.any(Throwable.class))).thenReturn(true);
		Assert.assertTrue(policy.canRetry(context));
		Assert.assertSame(classifier, policy.getClassifier());
	}

	@Test
	public void test_classify_nullClassifier() {
		RetryContext context = Mockito.mock(RetryContext.class);
		ClassifierSimpleRetryPolicy policy = new ClassifierSimpleRetryPolicy(null);
		Mockito.when(context.getLastThrowable()).thenReturn(new RuntimeException());
		Assert.assertFalse(policy.canRetry(context));
	}

	@Test
	public void test_classify_nullClassifier2() {
		RetryContext context = Mockito.mock(RetryContext.class);
		ClassifierSimpleRetryPolicy policy = new ClassifierSimpleRetryPolicy();
		Mockito.when(context.getLastThrowable()).thenReturn(new RuntimeException());
		Assert.assertFalse(policy.canRetry(context));
	}

	@Test
	public void test_classify_nullClassifier3() {
		RetryContext context = Mockito.mock(RetryContext.class);
		ClassifierSimpleRetryPolicy policy = new ClassifierSimpleRetryPolicy(4);
		Mockito.when(context.getLastThrowable()).thenReturn(new RuntimeException());
		Assert.assertFalse(policy.canRetry(context));
	}
}
