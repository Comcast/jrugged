/* Copyright 2009-2011 Comcast Interactive Media, LLC.

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

/**
 * Allows the user to map the standard {@link org.fishwife.jrugged.FlowRateExceededException}
 * thrown by a {@link org.fishwife.jrugged.ConstantFlowRegulator} into an application
 * specific exception.
 *
 * @param <T> is the application specific exception type.
 */
public interface ConstantFlowRegulatorExceptionMapper<T extends Exception> {

    /**
     * Turns a {@link org.fishwife.jrugged.FlowRateExceededException} into the desired exception (T)
     *
     * @param flowRegulator the {@link org.fishwife.jrugged.ConstantFlowRegulator}
     * @param e the <code>FlowRateExceededException</code> I get
     * @return the {@link Exception} I want thrown instead
     */
    public T map(ConstantFlowRegulator flowRegulator, FlowRateExceededException e);
    
}
