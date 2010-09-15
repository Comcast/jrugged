package org.fishwife.jrugged;

public class WrappedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WrappedException(Throwable cause) {
		super(cause);
	}
}
