/* ServiceWrapper.java
 * 
 * Copyright 2009-2010 Comcast Interactive Media, LLC.
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

import java.util.concurrent.Callable;

/** Several of the "rugged" code patterns in this library can be used to
 *  wrap an existing service in a "decorator" design pattern. This is the
 *  common interface provided by these wrapping classes that affect normal
 *  service calls.
 *  <p>
 *  If you are more into aspect-oriented programming (AOP), then you can
 *  also use the {@link ServiceWrapper} classes in your aspects.
 */
public interface ServiceWrapper {
    /** Wraps a {@link Callable} in some fashion.
     *  @param c the service call to wrap
     *  @return whatever <code>c</code> would normally return
     *  @throws Exception if <code>c</code> throws one
     */
    <T> T invoke(Callable<T> c) throws Exception;

    /** Wraps a {@link Runnable} in some fashion.
     *  @param r the service call/task to wrap
     *  @throws Exception if <code>r</code> throws one
     */
    void invoke(Runnable r) throws Exception;

    /** Wraps a {@link Runnable} task in some fashion, and returns a
     *  predetermined result on success.
     *  @param r the service call/task to wrap
     *  @param result what to return on success
     *  @return result
     *  @throws Exception if <code>r</code> throws one
     */
    <T> T invoke(Runnable r, T result) throws Exception;
}
