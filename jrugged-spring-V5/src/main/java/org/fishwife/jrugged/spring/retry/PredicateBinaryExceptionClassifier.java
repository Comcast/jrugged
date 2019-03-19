/* Copyright 2009-2019 Comcast Interactive Media, LLC.

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

package org.fishwife.jrugged.spring.retry;

import com.google.common.base.Predicate;
import org.springframework.classify.ClassifierSupport;

/***
 * A {@link Predicate} based classifier for {@link Throwable} objects which classifies them
 * as boolean values.
 */
public class PredicateBinaryExceptionClassifier extends ClassifierSupport<Throwable, Boolean> {

    private Predicate<Throwable> predicate;

    /***
     * Constructor.
     *
     * @param predicate The predicate to use to check the exception
     */
    public PredicateBinaryExceptionClassifier(Predicate<Throwable> predicate) {
        super(Boolean.TRUE);
        this.predicate = predicate;
    }

    /***
     * Get the predicate that is in use.
     *
     * @return the predicate
     */
    public Predicate<Throwable> getPredicate() {
        return predicate;
    }

    /***
     * Set the predicate that is in use
     * @param predicate the predicate
     */
    public void setPredicate(Predicate<Throwable> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Boolean classify(Throwable classifiable) {
        return predicate.apply(classifiable);
    }


}
