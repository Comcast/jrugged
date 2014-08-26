/* Copyright 2009-2012 Comcast Interactive Media, LLC.

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

import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryState;
import org.springframework.retry.support.RetryTemplate;

/***
 * Extended version of the {@link RetryTemplate} to allow easy use of the {@link Callable}
 * interface, instead of the {@link RetryCallback}
 */
public class ExtendedRetryTemplate extends RetryTemplate {

    /***
     * Constructor.
     */
    public ExtendedRetryTemplate() {
        super();
    }

    /***
     * Construct a {@link Callable} which wraps the given {@link RetryCallback},
     * and who's {@link java.util.concurrent.Callable#call()} method will execute
     * the callback via this {@link ExtendedRetryTemplate}
     *
     * @param callback The callback to wrap
     * @param <T> The return type of the callback
     * @return
     */
    public <T> Callable<T> asCallable(final RetryCallback<T, Exception> callback) {
        return new Callable<T>() {
            public T call() throws Exception {
               return ExtendedRetryTemplate.this.execute(callback);

            }
        };
    }

    /***
     * Construct a {@link Callable} which wraps the given {@link Callable},
     * and who's {@link java.util.concurrent.Callable#call()} method will execute
     * the callable via this {@link ExtendedRetryTemplate}
     *
     * @param callable The callable to wrap
     * @param <T> The return type of the callback
     * @return
     */
    public <T> Callable<T> asCallable(final Callable<T> callable) {
        return new Callable<T>() {
            public T call() throws Exception {
                return ExtendedRetryTemplate.this.execute(new RetryCallback<T, Exception>() {
                    public T doWithRetry(RetryContext retryContext) throws Exception {
                        return callable.call();
                    }
                });
            }
        };
    }

    /***
     * Execute a given {@link Callable} with retry logic.
     *
     * @param callable The callable to execute
     * @param <T> The return type of the callable
     * @return The result of the callable
     * @throws Exception
     * @throws ExhaustedRetryException If all retry attempts have been exhausted
     */
    public <T> T execute(final Callable<T> callable) throws Exception, ExhaustedRetryException {
        return execute(new RetryCallback<T, Exception>() {
            public T doWithRetry(RetryContext retryContext) throws Exception {
                return callable.call();
            }
        });
    }

    /***
     * Execute a given {@link Callable} with retry logic.
     *
     * @param callable The callable to execute
     * @param retryState The current retryState
     * @param <T> The return type of the callable
     * @return The result of the callable
     * @throws Exception
     * @throws ExhaustedRetryException If all retry attempts have been exhausted
     */
    public <T> T execute(final Callable<T> callable, RetryState retryState) throws Exception, ExhaustedRetryException {
        return execute(
                new RetryCallback<T, Exception>() {
                    public T doWithRetry(RetryContext retryContext) throws Exception {
                        return callable.call();
                    }
                },
                retryState);
    }

    /***
     * Execute a given {@link Callable} with retry logic.
     *
     * @param callable The callable to execute
     * @param recoveryCallback The recovery callback to execute when exceptions occur
     * @param retryState The current retryState
     * @param <T> The return type of the callable
     * @return The result of the callable
     * @throws Exception
     * @throws ExhaustedRetryException If all retry attempts have been exhausted
     */
    public <T> T execute(final Callable<T> callable, RecoveryCallback<T> recoveryCallback, RetryState retryState) throws Exception, ExhaustedRetryException {
        return execute(
                new RetryCallback<T, Exception>() {
                    public T doWithRetry(RetryContext retryContext) throws Exception {
                        return callable.call();
                    }
                },
                recoveryCallback,
                retryState);
    }

}
