/* Copyright 2009-2014 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.fishwife.jrugged.FailureInterpreter;
import org.fishwife.jrugged.SkepticBreakerConfig;
import org.fishwife.jrugged.BreakerFactory;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Surrounds methods decorated with the SkepticBreaker annotation with a named
 * {@link org.fishwife.jrugged.SkepticBreaker}.
 */
@Aspect
public class SkepticBreakerAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(SkepticBreakerAspect.class);

    /**
     * Maps names to SkepticBreakers.
     */
    private BreakerFactory skepticBreakerFactory;

    /** Default constructor. */
    public SkepticBreakerAspect() {
        skepticBreakerFactory = new BreakerFactory();
    }

    /**
     * Sets the {@link org.fishwife.jrugged.BreakerFactory} to use when creating new
     * {@link org.fishwife.jrugged.SkepticBreaker} instances.
     * @param skepticBreakerFactory the {@link org.fishwife.jrugged.BreakerFactory} to
     *   use.
     */
    public void setSkepticBreakerFactory(
            BreakerFactory skepticBreakerFactory) {
        this.skepticBreakerFactory = skepticBreakerFactory;
    }

    /**
     * Get the {@link org.fishwife.jrugged.BreakerFactory} that is being used to create
     * new {@link org.fishwife.jrugged.SkepticBreaker} instances.
     * @return the {@link org.fishwife.jrugged.BreakerFactory}.
     */
    public BreakerFactory getSkepticBreakerFactory() {
        return skepticBreakerFactory;
    }

    /** Runs a method call through the configured
     * {@link org.fishwife.jrugged.SkepticBreaker}.
     * @param pjp a {@link ProceedingJoinPoint} representing an annotated
     * method call.
     * @param skepticBreakerAnnotation the {@link org.fishwife.jrugged.SkepticBreaker} annotation
     * that wrapped the method.
     * @throws Throwable if the method invocation itself or the wrapping
     * {@link org.fishwife.jrugged.SkepticBreaker} throws one during execution.
     * @return The return value from the method call.
     */
    @Around("@annotation(skepticBreakerAnnotation)")
    public Object monitor(final ProceedingJoinPoint pjp,
              SkepticBreaker skepticBreakerAnnotation) throws Throwable {
        final String name = skepticBreakerAnnotation.name();

        org.fishwife.jrugged.SkepticBreaker skepticBreaker =
                skepticBreakerFactory.findSkepticBreaker(name);

        if (skepticBreaker == null) {
            DefaultFailureInterpreter dfi =
                    new DefaultFailureInterpreter(
                            skepticBreakerAnnotation.ignore(),
                            skepticBreakerAnnotation.limit(),
                            skepticBreakerAnnotation.windowMillis());

            SkepticBreakerConfig config = new SkepticBreakerConfig(skepticBreakerAnnotation.goodBase(), 
            		skepticBreakerAnnotation.goodMult(), skepticBreakerAnnotation.waitBase(), 
            		skepticBreakerAnnotation.waitMult(), skepticBreakerAnnotation.maxLevel(), dfi);

            skepticBreaker =
                    skepticBreakerFactory.createSkepticBreaker(name, config);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Have @SkepticBreaker method with breaker name {}, " +
                    "wrapping call on method {} of target object {} with status {}",
                    new Object[]{
                            name,
                            pjp.getSignature().getName(),
                            pjp.getTarget(),
                            skepticBreaker.getStatus()});
        }

        return skepticBreaker.invoke(new Callable<Object>() {
            public Object call() throws Exception {
                try {
                    return pjp.proceed();
                } catch (Throwable e) {
                    if (e instanceof Exception) {
                        throw (Exception) e;
                    } else if (e instanceof Error) {
                        throw (Error) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
