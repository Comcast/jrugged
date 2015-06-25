/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
import com.google.common.base.Predicates;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Collections;

/***
 * An extension to the existing {@link SimpleRetryPolicy} to allow for using an arbitrary
 * {@link Classifier} instance to determine if a given {@link Throwable} should trigger a
 * retry.
 */
public class ClassifierSimpleRetryPolicy
    extends SimpleRetryPolicy {

    private static final Predicate<Throwable> DEFAULT_PREDICATE = Predicates.alwaysFalse();
    private static final Classifier<Throwable, Boolean> DEFAULT_CLASSIFIER = new PredicateBinaryExceptionClassifier(DEFAULT_PREDICATE);

    private volatile Classifier<Throwable, Boolean> classifier;

    /***
     * Constructor.
     *
     * Uses the default values for the {@link #maxAttempts}
     * Uses the default classifier, which returns false for all exceptions.
     */
    public ClassifierSimpleRetryPolicy() {
        this(SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS, DEFAULT_CLASSIFIER);
    }

    /***
     * Constructor.

     * Uses the default classifier, which returns false for all exceptions.
     *
     * @param maxAttempts The maximum number of attempts allowed
     */
    public ClassifierSimpleRetryPolicy(int maxAttempts) {
        this(maxAttempts, DEFAULT_CLASSIFIER);
    }

    /***
     * Constructor.
     *
     * Uses the default values for the {@link #maxAttempts}
     *
     */
    public ClassifierSimpleRetryPolicy(Classifier<Throwable, Boolean> classifier) {
        this(SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS, classifier);
    }

    /***
     * Constructor.
     *
     * @param maxAttempts The maximum number of attempts allowed
     * @param classifier The classifier used to determine if an exception should trigger a retry
     */
    public ClassifierSimpleRetryPolicy(int maxAttempts, Classifier<Throwable, Boolean> classifier) {
        super(maxAttempts, Collections.EMPTY_MAP);
        this.classifier = classifier;
    }

    /***
     * Get the classifier instance.
     *
     * @return The classifier
     */
    public Classifier<Throwable, Boolean> getClassifier() {
        return classifier;
    }

    /***
     * Classify the exception as triggering a retry or not.
     *
     * @param throwable The exception which was thrown by the attempt.
     *
     * @return whether or not a retry should be attempted
     */
    private boolean classify(Throwable throwable) {
        return (classifier == null ? DEFAULT_CLASSIFIER : classifier).classify(throwable);
    }

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable t = context.getLastThrowable();
        return (t == null || classify(t)) && context.getRetryCount() < getMaxAttempts();
    }
}
