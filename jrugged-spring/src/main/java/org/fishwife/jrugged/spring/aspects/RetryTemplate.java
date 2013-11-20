package org.fishwife.jrugged.spring.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Annotation which enables the use of a RetryTemplate at the method level.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryTemplate {
    /***
     * The name of the spring bean where the appropriate retryTemplate can be found.
     * @return
     */
    String name() default "retryTemplate";

    /***
     * The name of the spring bean where a recovery callback method can be found.
     * @return
     */
    String recoveryCallbackName() default "";
}
