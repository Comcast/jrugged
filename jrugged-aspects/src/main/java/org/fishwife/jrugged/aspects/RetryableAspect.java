/* Copyright 2009-2013 Comcast Interactive Media, LLC.

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Surrounds methods decorated with the {@link org.fishwife.jrugged.aspects.Retryable} annotation
 * with logic to retry the method call.
 */
@Aspect
public class RetryableAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(RetryableAspect.class);

    /** Default constructor. */
    public RetryableAspect() {
    }

    /**
     * Runs a method call with retries.
     * @param pjp a {@link ProceedingJoinPoint} representing an annotated
     *            method call.
     * @param retryableAnnotation the {@link org.fishwife.jrugged.aspects.Retryable}
     *                            annotation that wrapped the method.
     * @throws Throwable if the method invocation itself throws one during execution.
     * @return The return value from the method call.
     */
    @Around("@annotation(retryableAnnotation)")
    public Object call(final ProceedingJoinPoint pjp,
			  Retryable retryableAnnotation) throws Throwable {
        final int maxRetries = retryableAnnotation.maxRetries();
        final long retryDelayMillies = retryableAnnotation.retryDelayMillis();
        final Class<? extends Throwable>[] retryOn = retryableAnnotation.retryOn();

        if (logger.isDebugEnabled()) {
            logger.debug("Have @Retryable method wrapping call on method {} of target object {}",
                    new Object[] {
                            pjp.getSignature().getName(),
                            pjp.getTarget()
                    });
        }

        int retryCount = 0;
        while (true) {
            try {
                return pjp.proceed();
            } catch (Throwable e) {

                if (shouldRetry(e, retryOn)) {
                    retryCount++;

                    if (retryCount <= maxRetries) {
                        if (retryDelayMillies > 0) {
                            delay(retryDelayMillies);
                        }
                        continue;
                    }
                }

                throw e;
            }
        }
    }

    private boolean shouldRetry(Throwable cause, Class<? extends Throwable>[] retryOn) {
        if (retryOn.length == 0) {
            return true;
        }

        for (Class<? extends Throwable> clazz : retryOn) {
            if (clazz.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }

    protected void delay(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            // Nothing much to do here.
        }
    }
}