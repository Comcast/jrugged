/* Copyright 2009 Comcast Interactive Media, LLC.

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
 * Invokes provided actions and reports success or failure.
 */
public interface FailureInterpreter {

    /**
     * Invokes provided <code>Callable</code>.
     * 
     * @param c
     * @return value returned from c
     * @throws CircuitShouldStayOpenException if an exception occurred, but calling 
     * circuit should stay open.
     * @throws Exception if an unacceptable exception occurred.
     */
    <V> V invoke(Callable<V> c) throws CircuitShouldStayOpenException,
            CircuitShouldBeClosedException, Exception;

}
