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
import com.google.common.base.Predicates;

/***
 * Additional predicates which can be useful.
 *
 */
public final class ExtendedPredicates {
    private ExtendedPredicates() {
        super();
    }

    /***
     * Create a predicate to see if a given object is an instance of a specific class, given that
     * all objects passed to this predicate will be a subclass of some parent of that class.
     *
     * @param superclazz The class which all objects passed to this predicate will be a subclass of
     * @param clazz The class to see if the passed in object will be an instanceof
     * @param <S> The superclass
     * @param <C> The class
     * @return The predicate
     */
    public static <S,C extends S> Predicate<S> isInstanceOf(final Class<S> superclazz, final Class<C> clazz) {
        return new Predicate<S>() {
            public boolean apply(S input) {
                return Predicates.instanceOf(clazz).apply(input);
            }
        };
    }

    /***
     * Create a predicate to check if a throwable's error message contains a specific string.
     *
     * @param expected The expected string
     * @param caseSensitive Is the comparison going to be case sensitive
     *
     * @return True if the throwable's message contains the expected string.
     */
    public static Predicate<Throwable> throwableContainsMessage(final String expected, final boolean caseSensitive) {
        return new Predicate<Throwable>() {
            public boolean apply(Throwable input) {
                String actual = input.getMessage();
                String exp = expected;
                if (! caseSensitive) {
                    actual = actual.toLowerCase();
                    exp = exp.toLowerCase();
                }
                return actual.contains(exp);
            }
        };
    }

}
