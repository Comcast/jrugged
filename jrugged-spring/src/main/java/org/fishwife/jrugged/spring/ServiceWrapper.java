package org.fishwife.jrugged.spring;

import java.util.concurrent.Callable;

import org.springframework.util.concurrent.ListenableFuture;

public interface ServiceWrapper extends org.fishwife.jrugged.ServiceWrapper {

	/**
	 * Wraps a {@link java.util.concurrent.Callable} in some fashion.
	 * 
	 * @param callable the service call to wrap
	 * @param          <T> The return value for a future call
	 * @return {@link ListenableFuture} of whatever <code>callable</code> would
	 *         normally return
	 * @throws Exception because it's part of the {@link Callable#call()} method
	 *                   signature. Exceptions must be handled in the callback
	 *                   methods.
	 */
	<T> ListenableFuture<T> invokeAsync(Callable<ListenableFuture<T>> callable) throws Exception;
}