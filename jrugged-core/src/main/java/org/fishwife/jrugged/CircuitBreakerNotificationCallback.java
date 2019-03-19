/* CircuitBreakerNotificationCallback.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fishwife.jrugged;

/**
 * Allows outside interested parties to register their interest in the state
 * changes of a {@link CircuitBreaker}. The callback allows the listeners to
 * then take action based on what the state change was.
 */
public abstract class CircuitBreakerNotificationCallback {

	/**
	 * The method needing an application specific implementation to deal with a
	 * change in {@link CircuitBreaker} state.
	 *
	 * @param s The new status of the {@link CircuitBreaker}
	 */
	public abstract void notify(Status s);
}
