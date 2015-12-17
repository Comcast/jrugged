package org.fishwife.jrugged.spring;

import java.util.concurrent.Callable;

import org.fishwife.jrugged.BreakerException;
import org.fishwife.jrugged.BreakerExceptionMapper;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;

public class AsyncSkepticBreaker extends org.fishwife.jrugged.SkepticBreaker {

    /** Creates a {@link AsyncSkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}). */
    public AsyncSkepticBreaker() {
    }

    /** Creates a {@link AsyncSkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and the default "tripped" exception
     *  behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link AsyncSkepticBreaker}.
     */
    public AsyncSkepticBreaker(String name) {
        this.name = name;
    }

    /** Creates a {@link AsyncSkepticBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public AsyncSkepticBreaker(FailureInterpreter fi) {
        failureInterpreter = fi;
    }

    /** Creates a {@link AsyncSkepticBreaker} with the specified {@link
     *    FailureInterpreter} and the default "tripped" exception
     *    behavior (throwing a {@link BreakerException}).
     *  @param name the name for the {@link AsyncSkepticBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     */
    public AsyncSkepticBreaker(String name, FailureInterpreter fi) {
        this.name = name;
        failureInterpreter = fi;
    }

    /** Creates a {@link AsyncSkepticBreaker} with a {@link
     *  DefaultFailureInterpreter} and using the supplied {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link AsyncSkepticBreaker}.
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public AsyncSkepticBreaker(String name, BreakerExceptionMapper<? extends Exception> mapper) {
        this.name = name;
        exceptionMapper = mapper;
    }

    /** Creates a {@link AsyncSkepticBreaker} with the provided {@link
     *  FailureInterpreter} and using the provided {@link
     *  BreakerExceptionMapper} when client calls are made
     *  while the breaker is tripped.
     *  @param name the name for the {@link AsyncSkepticBreaker}.
     *  @param fi the <code>FailureInterpreter</code> to use when
     *    determining whether a specific failure ought to cause the
     *    breaker to trip
     *  @param mapper helper used to translate a {@link
     *    BreakerException} into an application-specific one */
    public AsyncSkepticBreaker(String name,
        FailureInterpreter fi,
        BreakerExceptionMapper<? extends Exception> mapper) {
        this.name = name;
        failureInterpreter = fi;
        exceptionMapper = mapper;
    }

    /** Wrap the given service call with the {@link AsyncSkepticBreaker} protection logic.
     *  @param callable the {@link java.util.concurrent.Callable} to attempt
     *  @return {@link ListenableFuture} of whatever callable would return
     *  @throws org.fishwife.jrugged.BreakerException if the
     *    breaker was OPEN or HALF_CLOSED and this attempt wasn't the
     *    reset attempt
     *  @throws Exception if the {@link org.fishwife.jrugged.CircuitBreaker} is in OPEN state
     */
    public <T> ListenableFuture<T> invokeAsync(Callable<ListenableFuture<T>> callable) throws Exception {

        final SettableListenableFuture<T> response = new SettableListenableFuture<T>();
        ListenableFutureCallback<T> callback = new ListenableFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                close();
                response.set(result);
            }

            @Override
            public void onFailure(Throwable ex) {
                try {
                    handleFailure(ex);
                } catch (Exception e) {
                    response.setException(e);
                }
            }
        };

        if (!byPass) {
            if (!allowRequest()) {
                throw mapException(new BreakerException());
            }

            try {
                callable.call().addCallback(callback);
                return response;
            } catch (Throwable cause) {
                // This shouldn't happen because Throwables are handled in the async onFailure callback
                handleFailure(cause);
            }
            throw new IllegalStateException("not possible");
        }
        else {
            callable.call().addCallback(callback);
            return response;
        }
    }
}
