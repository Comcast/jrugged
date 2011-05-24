/* SystemClock.java
 *
 * Copyright 2009-2011 Comcast Interactive Media, LLC.
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
 * Provides current time as defined by the underlying operating system.
 */
public class SystemClock implements Clock {

	/**
	 * Returns the current system time in milliseconds.
	 * 
	 * @return the difference, measured in milliseconds, between the current
	 *         time and midnight, January 1, 1970 UTC.
	 * @see java.lang.System#currentTimeMillis()
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
