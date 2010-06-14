/* Copyright 2009-2010 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged;

import java.util.concurrent.Callable;

/**
 * A {@link FailureInterpreter} is a helper class that can be used by
 * a {@link CircuitBreaker} to determine whether a given failure
 * should cause the breaker to trip.
 */
public interface FailureInterpreter {

	/** Returns whether the governed {@link CircuitBreaker} should
	 * trip OPEN as a result of this failure.
	 * @param oops the {@link Throwable} failure that occurred
	 * @return boolean <code>true</code> iff the circuit should trip */
	boolean shouldTrip(Throwable oops);

}
