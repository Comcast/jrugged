package org.fishwife.jrugged.spring;

import java.util.concurrent.Callable;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;

public class RequestCounter extends org.fishwife.jrugged.RequestCounter implements ServiceWrapper {

    @Override
    public <T> ListenableFuture<T> invokeAsync(Callable<ListenableFuture<T>> callable) throws Exception {

        final SettableListenableFuture<T> response = new SettableListenableFuture<T>();
        ListenableFutureCallback<T> callback = new ListenableFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                succeed();
                response.set(result);
            }

            @Override
            public void onFailure(Throwable ex) {
                fail();
                response.setException(ex);
            }
        };

        callable.call().addCallback(callback);
        return response;
    }
}