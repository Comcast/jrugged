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
import org.fishwife.jrugged.CircuitBreakerConfig;
import org.fishwife.jrugged.BreakerFactory;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Surrounds methods decorated with the CircuitBreaker annotation with a named
 * {@link org.fishwife.jrugged.CircuitBreaker}.
 */
@Aspect
public class CircuitBreakerAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(CircuitBreakerAspect.class);

    /**
     * Maps names to CircuitBreakers.
     */
    private BreakerFactory circuitBreakerFactory;

    /** Default constructor. */
    public CircuitBreakerAspect() {
        circuitBreakerFactory = new BreakerFactory();
    }

    /**
     * Sets the {@link org.fishwife.jrugged.BreakerFactory} to use when creating new
     * {@link org.fishwife.jrugged.CircuitBreaker} instances.
     * @param circuitBreakerFactory the {@link org.fishwife.jrugged.BreakerFactory} to
     *   use.
     */
    public void setCircuitBreakerFactory(
            BreakerFactory circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    /**
     * Get the {@link org.fishwife.jrugged.BreakerFactory} that is being used to create
     * new {@link org.fishwife.jrugged.CircuitBreaker} instances.
     * @return the {@link org.fishwife.jrugged.BreakerFactory}.
     */
    public BreakerFactory getCircuitBreakerFactory() {
        return circuitBreakerFactory;
    }

    /** Runs a method call through the configured
     * {@link org.fishwife.jrugged.CircuitBreaker}.
     * @param pjp a {@link ProceedingJoinPoint} representing an annotated
     * method call.
     * @param circuitBreakerAnnotation the {@link org.fishwife.jrugged.CircuitBreaker} annotation
     * that wrapped the method.
     * @throws Throwable if the method invocation itself or the wrapping
     * {@link org.fishwife.jrugged.CircuitBreaker} throws one during execution.
     * @return The return value from the method call.
     */
    @Around("@annotation(circuitBreakerAnnotation)")
    public Object monitor(final ProceedingJoinPoint pjp,
              CircuitBreaker circuitBreakerAnnotation) throws Throwable {
        final String name = circuitBreakerAnnotation.name();

        org.fishwife.jrugged.CircuitBreaker circuitBreaker =
                circuitBreakerFactory.findCircuitBreaker(name);

        if (circuitBreaker == null) {
            DefaultFailureInterpreter dfi =
                    new DefaultFailureInterpreter(
                            circuitBreakerAnnotation.ignore(),
                            circuitBreakerAnnotation.limit(),
                            circuitBreakerAnnotation.windowMillis());

            CircuitBreakerConfig config = new CircuitBreakerConfig(
                    circuitBreakerAnnotation.resetMillis(), dfi);

            circuitBreaker =
                    circuitBreakerFactory.createCircuitBreaker(name, config);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Have @CircuitBreaker method with breaker name {}, " +
                    "wrapping call on method {} of target object {} with status {}",
                    new Object[]{
                            name,
                            pjp.getSignature().getName(),
                            pjp.getTarget(),
                            circuitBreaker.getStatus()});
        }

        return circuitBreaker.invoke(new Callable<Object>() {
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
