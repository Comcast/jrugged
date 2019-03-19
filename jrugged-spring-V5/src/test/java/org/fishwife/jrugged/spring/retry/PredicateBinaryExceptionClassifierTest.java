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
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PredicateBinaryExceptionClassifierTest {

    @Test
    public void testClassify() {
        Predicate predicate = Mockito.mock(Predicate.class);
        PredicateBinaryExceptionClassifier classifier = new PredicateBinaryExceptionClassifier(predicate);
        classifier.setPredicate(predicate);
        Mockito.when(predicate.apply(Mockito.any(Throwable.class))).thenReturn(false);
        Assert.assertSame(predicate, classifier.getPredicate());
        Assert.assertFalse(classifier.classify(new RuntimeException()));
        Mockito.verify(predicate, Mockito.times(1)).apply(Mockito.any(Throwable.class));
    }
}
